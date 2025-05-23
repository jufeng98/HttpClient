package org.javamaster.httpclient.scan

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.jetbrains.rd.util.concurrentMapOf
import org.javamaster.httpclient.scan.support.ControllerPsiModificationTracker
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.scan.support.SpringControllerScanService
import java.util.function.Consumer

/**
 * @author yudong
 */
object ScanRequest {
    private val keyMap = concurrentMapOf<String, Key<CachedValue<Map<String, List<Request>>>>>()

    fun findApiMethod(module: Module, searchTxt: String, method: String): PsiMethod? {
        val requestMap = getCacheRequestMap(module, module.project)

        val requests = requestMap["$searchTxt-$method"] ?: return null

        // There may be more than one controller method here, so for simplicity, take the first one directly,
        // without making complex judgments based on the mapping rules of SpringMVC
        val request = requests[0]

        return request.psiElement
    }

    fun fetchRequests(project: Project, searchScope: GlobalSearchScope, consumer: Consumer<Request>) {
        val controllerScanService = SpringControllerScanService.getService(project)

        controllerScanService.fetchRequests(project, searchScope, consumer)
    }

    fun getCacheRequestMap(module: Module, project: Project): Map<String, List<Request>> {
        val controllerScanService = SpringControllerScanService.getService(project)

        val key = keyMap.computeIfAbsent(module.name) {
            Key.create("httpClient.requestMap.$it")
        }

        return CachedValuesManager.getManager(project)
            .getCachedValue(module, key, {
                val requests = controllerScanService.findRequests(project, module.moduleWithLibrariesScope)

                val requestMap = requests.groupBy { it.toString() }

                CachedValueProvider.Result.create(requestMap, ControllerPsiModificationTracker)

            }, false)
    }

}
