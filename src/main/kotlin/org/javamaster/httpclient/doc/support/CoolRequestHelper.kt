package org.javamaster.httpclient.doc.support

import com.cool.request.components.http.Controller
import com.cool.request.scan.Scans
import com.cool.request.view.tool.search.ControllerNavigationItem
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
object CoolRequestHelper {
    private var allController: MutableList<out Any>? = null

    private fun getAllController(project: Project): MutableList<out Any> {
        if (allController != null) {
            return allController!!
        }

        allController = mutableListOf()

        val result = mutableListOf<Controller>()

        Scans.getInstance(project).scanController(project, null, result)

        allController = result.stream()
            .map {
                ControllerNavigationItem(it, project)
            }
            .toList()

        return allController!!
    }

    fun findModule(requestTarget: HttpRequestTarget, virtualFile: VirtualFile): Module? {
        return if (HttpUtils.isFileInIdeaDir(virtualFile)) {
            HttpUtils.getOriginalModule(requestTarget)
        } else {
            ModuleUtil.findModuleForPsiElement(requestTarget)
        }
    }


    fun findMethod(module: Module, searchTxt: String): PsiMethod? {
        val allController = getAllController(module.project)

        val controllerNavigationItem = findControllerNavigationItem(allController, searchTxt)

        val psiMethods = findControllerPsiMethods(controllerNavigationItem, module)
        if (psiMethods.isEmpty()) {
            return null
        }

        return psiMethods[0]
    }

    fun findControllerNavigationItem(controllers: MutableList<out Any>, searchTxt: String): ControllerNavigationItem {
        return if (controllers.size == 1) {
            controllers[0] as ControllerNavigationItem
        } else {
            val urlMap = controllers.groupBy {
                val navigationItem = it as ControllerNavigationItem
                navigationItem.url
            }
            val itemList = urlMap[searchTxt]
            if (itemList.isNullOrEmpty()) {
                controllers[0] as ControllerNavigationItem
            } else {
                itemList[0] as ControllerNavigationItem
            }
        }
    }

    fun findControllerPsiMethods(
        navigationItem: ControllerNavigationItem,
        module: Module,
    ): Array<out PsiMethod> {
        val controllerFullClassName = navigationItem.javaClassName
        val controllerMethodName = navigationItem.methodName

        return HttpUtils.findControllerPsiMethods(controllerFullClassName, controllerMethodName, module)
    }

}
