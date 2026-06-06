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
import org.javamaster.httpclient.logger.logInfo
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.scan.support.SpringControllerScanService
import org.javamaster.httpclient.utils.SpringUtils
import java.util.concurrent.ConcurrentMap

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class ScanRequest(private val project: Project) {
    val controllerScanService = SpringControllerScanService.getService(project)

    /**
     * 模块名 -> 控制器类全限定名 -> 请求列表
     */
    private val moduleControllerMap = concurrentMapOf<String, ConcurrentMap<String, List<Request>>>()

    fun findCacheApiMethod(module: Module, searchTxt: String, method: String): PsiMethod? {
        val requestMap = getCacheRequestPathMethodMap(module)

        val requests = requestMap["$searchTxt-$method"] ?: return null

        // There may be more than one controller method here, so for simplicity, take the first one directly,
        // without making complex judgments based on the mapping rules of SpringMVC
        return requests.firstOrNull()?.psiElement
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
            // 尝试初始化模块的请求并缓存
            getCacheRequestMap(module)

            return
        }

        for (psiClass in javaFile.classes) {
            val qualifiedName = psiClass.qualifiedName ?: continue

            if (cacheRequestMap.containsKey(qualifiedName) || SpringUtils.isSpringController(javaFile)) {
                val requestsNew = controllerScanService.findRequests(project, GlobalSearchScope.fileScope(javaFile))

                val map = requestsNew.groupBy { it.controllerClassQualifiedName }

                logInfo("完成扫描文件 ${javaFile.name} 的请求,共 ${map.size} 个控制器类,共 ${requestsNew.size} 个请求")

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

        return moduleControllerQualifiedNameMap.values
            .flatten()
    }

    fun getCacheRequestPathMethodMap(module: Module): Map<String, List<Request>> {
        val requests = getCacheRequestList(module)

        return requests.groupBy { it.toString() }
    }

}
