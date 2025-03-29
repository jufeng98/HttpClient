package org.javamaster.httpclient.doc

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.java.JavaDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.utils.HttpUtils.generateAnno

/**
 * 悬浮提示出入参的 json key 对应的 SpringMVC Controller 或 Dubbo Service 方法字段
 *
 * @author yudong
 */
class JsonKeyMethodFieldDocumentationProvider : DocumentationProvider {

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int,
    ): PsiElement? {
        val util = TargetElementUtil.getInstance()
        val element = util.findTargetElement(editor, util.allAccepted, targetOffset)

        if (element is PsiField) {
            return MyPsiField(element)
        }

        return element
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element !is MyPsiField) {
            return null
        }

        val psiField = element.psiField

        val str = JavaDocumentationProvider.generateExternalJavadoc(psiField, null)

        val annotation = psiField.getAnnotation("io.swagger.annotations.ApiModelProperty")
        return if (annotation != null) {
            val generateAnno = generateAnno(annotation)
            str + generateAnno
        } else {
            str
        }
    }

    private class MyPsiField(val psiField: PsiField) : ASTWrapperPsiElement(psiField.node)

}
