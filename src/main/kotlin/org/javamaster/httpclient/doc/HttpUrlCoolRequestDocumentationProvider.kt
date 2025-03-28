package org.javamaster.httpclient.doc

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.doc.support.CoolRequestHelper
import org.javamaster.httpclient.reference.support.HttpControllerMethodPsiElement

/**
 * 悬浮提示对应的 Controller 方法
 *
 * @author yudong
 */
class HttpUrlCoolRequestDocumentationProvider : DocumentationProvider {

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is HttpControllerMethodPsiElement) {
            return null
        }

        val requestTarget = element.requestTarget
        val virtualFile = element.containingFile.virtualFile

        val module = CoolRequestHelper.findModule(requestTarget, virtualFile) ?: return null

        val psiMethod = CoolRequestHelper.findMethod(module, element.searchTxt) ?: return null

        return JavaDocumentationProvider.generateExternalJavadoc(psiMethod, null)
    }

}
