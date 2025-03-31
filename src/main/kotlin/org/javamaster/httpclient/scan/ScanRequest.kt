package org.javamaster.httpclient.scan

import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.javamaster.httpclient.scan.support.ControllerPsiModificationTracker
import org.javamaster.httpclient.scan.support.Request
import org.javamaster.httpclient.scan.support.SpringControllerScanService

/**
 * @author yudong
 */
object ScanRequest {
    private val requestMapKey = Key.create<CachedValue<Map<String, List<Request>>>>("httpClient.requestMap")

    fun findMethod(module: Module, searchTxt: String, method: String): PsiMethod? {
        val requestMap = getCacheRequestMap(module, null)

        val requests = requestMap["$searchTxt-$method"] ?: return null

        // 这里可能有多个控制器方法,简单起见直接取第一个,不在根据 SpringMVC 的映射规则做复杂判断
        val request = requests[0]

        return request.psiElement
    }

    fun getCacheRequestMap(module: Module, progressIndicator: ProgressIndicator?): Map<String, List<Request>> {
        val project = module.project
        val controllerScanService = SpringControllerScanService.getService(project)

        return CachedValuesManager.getManager(project)
            .getCachedValue(module, requestMapKey, {
                val requests = controllerScanService.getSpringMvcRequest(project, module, progressIndicator)

                val requestMap = requests.groupBy { it.toString() }

                CachedValueProvider.Result.create(requestMap, ControllerPsiModificationTracker)

            }, false)
    }

}
