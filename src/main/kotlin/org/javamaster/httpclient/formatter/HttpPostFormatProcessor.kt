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

    override fun processElement(
        p0: PsiElement,
        p1: CodeStyleSettings,
    ): PsiElement {
        return p0
    }

    override fun processText(
        file: PsiFile,
        textRange: TextRange,
        p2: CodeStyleSettings,
    ): TextRange {
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
            .map {
                PsiTreeUtil.findChildrenOfType(it.request, HttpMessageBody::class.java)
            }
            .flatten()
            .filter {
                var parent = it.parent?.parent ?: return@filter false

                if (parent is HttpMultipartField) {
                    return@filter parent.contentType == ContentType.APPLICATION_FORM_URLENCODED
                }

                parent = parent.parent
                if (parent is HttpRequest) {
                    return@filter parent.contentType == ContentType.APPLICATION_FORM_URLENCODED
                }

                false
            }

        messageBodies.forEach {
            val queryParam = it.text
            val split = queryParam.split("&")

            val queryParamNew = split.joinToString(" &\n") {
                val list = it.split("=")
                list[0].trim() + " = " + list[1].trim()
            }

            val messageBody = HttpPsiFactory.createMessageBody(project, queryParamNew)
            it.replace(messageBody)
        }

        return topLevelFile.textRange
    }
}
