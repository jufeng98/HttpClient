package org.javamaster.httpclient.doc

import com.cool.request.components.http.Controller
import com.cool.request.scan.Scans
import com.cool.request.view.tool.search.ControllerNavigationItem
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.reference.support.HttpControllerMethodPsiElement
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.findControllerNavigationItem

/**
 * @author yudong
 */
class HttpDocumentationProvider : DocumentationProvider {
    private var allController: MutableList<out Any>? = null


    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is HttpControllerMethodPsiElement) {
            return null
        }

        val requestTarget = element.requestTarget
        val virtualFile = element.containingFile.virtualFile
        val module = if (HttpUtils.isFileInIdeaDir(virtualFile)) {
            HttpUtils.getOriginalModule(requestTarget)
        } else {
            ModuleUtil.findModuleForPsiElement(requestTarget)
        } ?: return null

        val project = element.project
        val result = mutableListOf<Controller>()


        if (allController == null) {
            Scans.getInstance(project).scanController(project, null, result)

            allController = result.stream()
                .map {
                    ControllerNavigationItem(it, project)
                }
                .toList()
        }

        val controllerNavigationItem = findControllerNavigationItem(allController!!, element.searchTxt)

        val psiMethods = HttpUtils.findControllerPsiMethods(controllerNavigationItem, module)
        if (psiMethods.isEmpty()) {
            return null
        }

        val psiMethod = psiMethods[0]

        return JavaDocumentationProvider.generateExternalJavadoc(psiMethod, null)
    }

}
