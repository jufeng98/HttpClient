package org.javamaster.httpclient.scan

import com.google.common.collect.Maps
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.rd.util.concurrentMapOf
import org.javamaster.httpclient.enums.HttpMethod
import org.javamaster.httpclient.logger.HttpRequestLogger.logInfo
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.scan.support.SpringControllerScanService
import org.javamaster.httpclient.utils.SpringUtils
import java.util.concurrent.ConcurrentMap

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class ScanRequest(private val project: Project) {
    private val controllerScanService = SpringControllerScanService.getService(project)

    /**
     * 模块名 -> 控制器类全限定名 -> 请求列表
     */
    private val moduleControllerMap = concurrentMapOf<String, ConcurrentMap<String, List<Request>>>()

    /**
     * 查找 Spring MVC 控制器的方法
     *
     * @param module 模块
     * @param path 调用路径，例如 /api/user
     * @param method 调用方法名，例如 GET、POST等
     */
    fun findSpringMvcMethod(module: Module, path: String, method: String): PsiMethod? {
        val allRequests = getCacheRequestList(module)

        val methodMap = allRequests.groupBy { it.method.name }

        // 按方法名先匹配
        val requests = methodMap[method] ?: return null

        val pathMap = requests.groupBy { it.path }

        // 为提高效率, 先根据路径完全匹配规则来查找
        val request = pathMap[path]?.firstOrNull()
        if (request != null) {
            return request.psiElement
        }

        // 有可能带有 contextPath 或者网关的前缀,先尝试去掉第一段来匹配
        val firstIdx = path.indexOf("/", 1)
        if (firstIdx != -1) {
            val pathWithoutFirst = path.substring(firstIdx)
            val requestWithoutFirst = pathMap[pathWithoutFirst]?.firstOrNull()
            if (requestWithoutFirst != null) {
                return requestWithoutFirst.psiElement
            }

            // 在尝试去掉第二段来匹配
            val secondIdx = pathWithoutFirst.indexOf("/", 1)
            if (secondIdx != -1) {
                val pathWithoutSecond = pathWithoutFirst.substring(secondIdx)
                val requestWithoutSecond = pathMap[pathWithoutSecond]?.firstOrNull()
                if (requestWithoutSecond != null) {
                    return requestWithoutSecond.psiElement
                }
            }
        }

        // 模式匹配（较慢，作为降级策略）
        for (requestTmp in requests) {
            if (SpringUtils.matchPath(requestTmp.path, path)) {
                return requestTmp.psiElement
            }
        }

        return null
    }

    fun getCacheRequestList(filterMethods: MutableSet<HttpMethod>): MutableList<Request>? {
        if (moduleControllerMap.isEmpty()) {
            return null
        }

        val requests = mutableListOf<Request>()

        for (concurrentMap in moduleControllerMap.values) {
            for (entry in concurrentMap) {
                for (request in entry.value) {
                    if (request.psiElement == null || !request.psiElement.isValid || !filterMethods.contains(request.method)) continue

                    requests.add(request)

                    if (requests.size >= 30) {
                        return requests
                    }

                }
            }
        }

        return null
    }

    fun handleFileChange(javaFile: PsiJavaFile, module: Module) {
        val moduleName = module.name

        var cacheRequestMap = moduleControllerMap[moduleName]
        if (cacheRequestMap == null) {
            // 初始化模块的请求并缓存
            getCacheRequestMap(module)

            return
        }

        for (psiClass in javaFile.classes) {
            val qualifiedName = psiClass.qualifiedName ?: continue

            if (cacheRequestMap.containsKey(qualifiedName) || SpringUtils.isSpringController(javaFile)) {
                val requestsNew = controllerScanService.findRequests(project, GlobalSearchScope.fileScope(javaFile))

                val map = requestsNew.groupBy { it.controllerClassQualifiedName }

                cacheRequestMap.putAll(map)

                break
            }
        }
    }

    fun getCacheRequestMap(module: Module): ConcurrentMap<String, List<Request>> {
        return moduleControllerMap.computeIfAbsent(module.name) {
            val requests = controllerScanService.findRequests(project, module.moduleWithLibrariesScope)

            val map = requests.groupBy { it.controllerClassQualifiedName }

            logInfo("完成扫描模块 ${module.name} 的请求,共 ${map.size} 个控制器类,共 ${requests.size} 个请求")

            val currentMap = Maps.newConcurrentMap<String, List<Request>>()

            currentMap.putAll(map)

            currentMap
        }
    }

    fun getCacheRequestList(module: Module): List<Request> {
        val moduleControllerQualifiedNameMap = getCacheRequestMap(module)

        return moduleControllerQualifiedNameMap.values.flatten()
    }

}
