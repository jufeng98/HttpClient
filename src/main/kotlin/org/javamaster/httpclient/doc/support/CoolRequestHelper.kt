package org.javamaster.httpclient.doc.support

import com.cool.request.components.http.Controller
import com.cool.request.scan.Scans
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
object CoolRequestHelper {
    private val key = Key.create<CachedValue<Map<String, List<Controller>>>>("httpClient.coolRequest.controllers")

    private fun getCacheControllerMap(project: Project): Map<String, List<Controller>> {
        return CachedValuesManager.getManager(project)
            .getCachedValue(project, key, {
                val controllers: MutableList<Controller> = ArrayList(1500)

                Scans.getInstance(project).scanController(project, null, controllers)

                val controllerMap = controllers.groupBy { it.httpMethod + "-" + it.url }

                CachedValueProvider.Result.create(controllerMap, ControllerPsiModificationTracker)

            }, false)
    }

    fun findMethod(module: Module, searchTxt: String, method: String): PsiMethod? {
        val controllerMap = getCacheControllerMap(module.project)

        val controllers = controllerMap["$method-$searchTxt"] ?: return null

        // 这里可能有多个控制器方法,简单起见直接取第一个,不在根据 SpringMVC 的映射规则做复杂判断
        val controller = controllers[0]

        val psiMethods = findControllerPsiMethods(controller, module)
        if (psiMethods.isEmpty()) {
            return null
        }

        return psiMethods[0]
    }

    private fun findControllerPsiMethods(controller: Controller, module: Module): Array<PsiMethod> {
        val controllerFullClassName = controller.javaClassName
        val controllerMethodName = controller.methodName

        return HttpUtils.findControllerPsiMethods(controllerFullClassName, controllerMethodName, module)
    }

}
