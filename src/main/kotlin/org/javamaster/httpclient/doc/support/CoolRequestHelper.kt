package org.javamaster.httpclient.doc.support

import com.cool.request.components.http.Controller
import com.cool.request.scan.Scans
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
object CoolRequestHelper {
    private val key = Key.create<CachedValue<MutableList<Controller>>>("httpClient.coolRequest.controllers")

    private fun getCacheControllers(project: Project): MutableList<Controller> {
        return CachedValuesManager.getManager(project)
            .getCachedValue(project, key, {
                val controllers: MutableList<Controller> = mutableListOf()

                Scans.getInstance(project).scanController(project, null, controllers)

                CachedValueProvider.Result.create(controllers, PsiModificationTracker.MODIFICATION_COUNT)

            }, false)
    }

    fun findModule(requestTarget: HttpRequestTarget, virtualFile: VirtualFile): Module? {
        return if (HttpUtils.isFileInIdeaDir(virtualFile)) {
            HttpUtils.getOriginalModule(requestTarget)
        } else {
            ModuleUtil.findModuleForPsiElement(requestTarget)
        }
    }


    fun findMethod(module: Module, searchTxt: String, method: String): PsiMethod? {
        val allControllers = getCacheControllers(module.project)

        val controller = findMatchedController(allControllers, searchTxt, method) ?: return null

        val psiMethods = findControllerPsiMethods(controller, module)
        if (psiMethods.isEmpty()) {
            return null
        }

        return psiMethods[0]
    }

    private fun findMatchedController(
        controllers: MutableList<Controller>,
        searchTxt: String,
        method: String,
    ): Controller? {
        return controllers.firstOrNull {
            it.httpMethod == method && it.url == searchTxt
        }
    }

    private fun findControllerPsiMethods(controller: Controller, module: Module): Array<PsiMethod> {
        val controllerFullClassName = controller.javaClassName
        val controllerMethodName = controller.methodName

        return HttpUtils.findControllerPsiMethods(controllerFullClassName, controllerMethodName, module)
    }

}
