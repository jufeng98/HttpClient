package org.javamaster.httpclient.inject

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiUtil
import com.intellij.util.SmartList
import com.intellij.util.containers.ContainerUtil
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.consts.HttpConsts.Companion.RES_SIZE_LIMIT
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpMultipartField
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.InjectionUtils

/**
 * @author yudong
 */
class MessageBodyInjectionContributor : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val virtualFile = PsiUtil.getVirtualFile(context)
        val host = context as PsiLanguageInjectionHost

        if (virtualFile != null) {
            val contentType = virtualFile.getUserData(HttpConsts.httpDashboardContentTypeKey)
            if (contentType != null) {
                tryInject(contentType, host, registrar)
                return
            }
        }

        var contentType: ContentType? = null
        val tmpParent = context.parent.parent
        val parent = tmpParent?.parent
        if (parent is HttpRequest) {
            contentType = parent.contentType
        } else if (tmpParent is HttpMultipartField) {
            contentType = tmpParent.contentType
        }

        tryInject(contentType?.mimeType, host, registrar)
    }

    private fun tryInject(contentType: String?, host: PsiLanguageInjectionHost, registrar: MultiHostRegistrar) {
        val mimeType = contentType ?: ContentType.TEXT_PLAIN.mimeType

        val languages = Language.findInstancesByMimeType(mimeType)
        val language = ContainerUtil.getFirstItem(languages) ?: PlainTextLanguage.INSTANCE

        val text = host.text
        val length = text.length
        if (length > RES_SIZE_LIMIT) {
            // 大文本不再注入语言，防止 IDEA 卡顿
            return
        }

        if (length > 8000 && text.indexOf('\n', length - 8000) == -1) {
            // 表明是未格式化的过长的文本，注入语言也没有意义，跳过注入
            return
        }

        if (language == PlainTextLanguage.INSTANCE) {
            val textRange = InjectionUtils.innerRange(host) ?: return

            registrar.startInjecting(language)
            registrar.addPlace(null, null, host, textRange)
            registrar.doneInjecting()
            return
        }

        registrar.startInjecting(language)

        val variableRanges = injectBody(registrar, host, text)

        registrar.doneInjecting()

        if (variableRanges.isNotEmpty()) {
            registrar.startInjecting(PlainTextLanguage.INSTANCE)

            variableRanges.forEach {
                registrar.addPlace(null, null, host, it)
            }

            registrar.doneInjecting()
        }
    }

    private fun injectBody(
        registrar: MultiHostRegistrar,
        host: PsiLanguageInjectionHost,
        messageText: String,
    ): MutableList<TextRange> {
        var lastVariableRangeEndOffset = 0

        val variablesRanges = HttpPsiUtils.collectVariablesRangesInMessageBody(messageText)

        for (variableRange in variablesRanges) {
            val range = TextRange.create(lastVariableRangeEndOffset, variableRange.startOffset)

            registrar.addPlace(null, "0", host, range)

            lastVariableRangeEndOffset = variableRange.endOffset
        }

        val range = TextRange.create(lastVariableRangeEndOffset, messageText.length)

        registrar.addPlace(null, null, host, range.shiftRight(0))

        return variablesRanges
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return SmartList(HttpMessageBody::class.java)
    }

}
