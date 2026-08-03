package org.javamaster.httpclient.scan

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.jetbrains.rd.util.concurrentMapOf
import org.javamaster.httpclient.logger.HttpRequestLogger.logInfo
import org.javamaster.httpclient.scan.support.ModuleFileModificationTracker
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.scan.support.SpringControllerScanService
import org.javamaster.httpclient.utils.SpringUtils

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class ScanRequest(project: Project) {
    private val controllerScanService = SpringControllerScanService.getService(project)

    private val keyMap = concurrentMapOf<String, Key<CachedValue<Map<String, List<Request>>>>>()

    private val modificationTrackerMap = concurrentMapOf<String, ModuleFileModificationTracker>()

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

    fun getModificationTracker(module: Module): ModuleFileModificationTracker {
        return modificationTrackerMap.computeIfAbsent(module.name) {
            ModuleFileModificationTracker()
        }
    }

    fun getCacheRequestMap(module: Module): Map<String, List<Request>> {
        val moduleKey = keyMap.computeIfAbsent(module.name) {
            Key.create("httpClient.requestMap.$it")
        }

        val modificationTracker = getModificationTracker(module)

        return CachedValuesManager.getManager(module.project)
            .getCachedValue(module, moduleKey, {
                val requests = controllerScanService.findRequests(module.moduleWithLibrariesScope)

                val requestMap = requests.groupBy { it.controllerClassQualifiedName }

                logInfo("完成扫描模块 ${module.name} 的请求,共 ${requestMap.size} 个控制器类,共 ${requests.size} 个请求")

                CachedValueProvider.Result.create(requestMap, modificationTracker)
            }, false)
    }

    fun getCacheRequestList(module: Module): List<Request> {
        val moduleControllerQualifiedNameMap = getCacheRequestMap(module)

        return moduleControllerQualifiedNameMap.values.flatten()
    }

}
