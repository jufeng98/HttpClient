package org.javamaster.httpclient.doc

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.javamaster.httpclient.utils.HttpUtils.API_OPERATION_ANNO_NAME
import org.javamaster.httpclient.utils.HttpUtils.generateAnno

/**
 * show SpringMVC Controller method information when hover in url
 *
 * @author yudong
 */
class HttpUrlControllerMethodDocumentationProvider : DocumentationProvider {

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int,
    ): PsiElement? {
        val util = TargetElementUtil.getInstance()
        val element = util.findTargetElement(editor, util.allAccepted, targetOffset)

        if (element is PsiMethod) {
            return MyPsiMethod(element)
        }

        return element
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is MyPsiMethod) {
            return null
        }

        val psiMethod = element.psiMethod

        val str = JavaDocumentationProvider.generateExternalJavadoc(psiMethod, null)

        val annotation = psiMethod.getAnnotation(API_OPERATION_ANNO_NAME)
        return if (annotation != null) {
            val generateAnno = generateAnno(annotation)
            str + generateAnno
        } else {
            str
        }
    }

    private class MyPsiMethod(val psiMethod: PsiMethod) : ASTWrapperPsiElement(psiMethod.node)

}
