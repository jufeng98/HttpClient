package org.javamaster.httpclient.formatter

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.util.PsiTreeUtil
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.factory.HttpPsiFactory
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpMultipartField
import org.javamaster.httpclient.psi.HttpRequest

/**
 * @author yudong
 */
class HttpPostFormatProcessor : PostFormatProcessor {

    override fun processElement(psiElement: PsiElement, settings: CodeStyleSettings): PsiElement {
        return psiElement
    }

    override fun processText(file: PsiFile, textRange: TextRange, settings: CodeStyleSettings): TextRange {
        val project = file.project
        val topLevelFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(file)
        if (topLevelFile !is HttpFile) {
            return textRange
        }

        val messageBodies = topLevelFile.getRequestBlocks()
            .filter {
                val request = it.request ?: return@filter false

                HttpRequestEnum.getInstance(request.method.text) == HttpRequestEnum.POST
            }
            .map { PsiTreeUtil.findChildrenOfType(it.request, HttpMessageBody::class.java) }
            .flatten()
            .filter {
                var parent = it.parent?.parent ?: return@filter false

                if (parent is HttpMultipartField) {
                    return@filter parent.contentType?.mimeType == ContentType.APPLICATION_FORM_URLENCODED.mimeType
                }

                parent = parent.parent
                if (parent is HttpRequest) {
                    return@filter parent.contentType?.mimeType == ContentType.APPLICATION_FORM_URLENCODED.mimeType
                }

                false
            }

        messageBodies.forEach {
            val queryParam = it.text
            val queryParamFormat = queryParam.split("&").joinToString(" &\n") {
                val list = it.split("=")
                if (list.size == 1) {
                    return@joinToString list[0].trim()
                }

                list[0].trim() + " = " + list[1].trim()
            }

            val messageBody = HttpPsiFactory.createMessageBody(project, queryParamFormat)
            it.replace(messageBody)
        }

        return topLevelFile.textRange
    }
}
