package org.javamaster.httpclient.scan

import com.google.common.collect.Maps
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.rd.util.concurrentMapOf
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.scan.support.SpringControllerScanService
import org.javamaster.httpclient.utils.SpringUtils
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class ScanRequest {
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

    fun isCacheMapInit(): Boolean {
        return moduleControllerMap.isNotEmpty()
    }

    fun fetchCacheRequestList(consumer: Consumer<Request>) {
        for (concurrentMap in moduleControllerMap.values) {
            for (entry in concurrentMap) {
                for (request in entry.value) {
                    consumer.accept(request)
                }
            }
        }
    }

    fun handleFileChange(javaFile: PsiJavaFile, module: Module) {
        val project = module.project
        val controllerScanService = SpringControllerScanService.getService(project)

        val cacheRequestMap = getCacheRequestMap(module)

        for (psiClass in javaFile.classes) {
            val qualifiedName = psiClass.qualifiedName ?: continue

            if (cacheRequestMap.containsKey(qualifiedName) || SpringUtils.isSpringController(javaFile)) {
                val requestsNew = controllerScanService.findRequests(project, GlobalSearchScope.fileScope(javaFile))

                val map = requestsNew.groupBy { it.controllerClassQualifiedName }

                map.forEach {
                    cacheRequestMap[it.key] = it.value
                }

                break
            }
        }
    }

    fun getCacheRequestMap(module: Module): ConcurrentMap<String, List<Request>> {
        val project = module.project
        val controllerScanService = SpringControllerScanService.getService(project)

        return moduleControllerMap.computeIfAbsent(module.name) {
            val requests = controllerScanService.findRequests(project, module.moduleWithLibrariesScope)

            val map = requests.groupBy { it.controllerClassQualifiedName }

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
