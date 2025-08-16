package org.javamaster.httpclient.inject

import com.intellij.json.JsonLanguage
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
import org.javamaster.httpclient.action.dashboard.view.ContentTypeActionGroup
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpMultipartField
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.InjectionUtils

/**
 * @author yudong
 */
class MessageBodyInjectionContributor : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val virtualFile = PsiUtil.getVirtualFile(context)
        val host = context as PsiLanguageInjectionHost

        if (virtualFile != null) {
            val contentType = virtualFile.getUserData(ContentTypeActionGroup.httpDashboardContentTypeKey)
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

        tryInject(contentType, host, registrar)
    }

    private fun tryInject(contentType: ContentType?, host: PsiLanguageInjectionHost, registrar: MultiHostRegistrar) {
        val mimeType = contentType?.mimeType ?: ContentType.TEXT_PLAIN.mimeType

        val languages = Language.findInstancesByMimeType(mimeType)
        val language = ContainerUtil.getFirstItem(languages) ?: PlainTextLanguage.INSTANCE

        val text = host.text
        if (text.length > HttpUtils.RES_TEXT_SIZE_LIMIT) {
            // 大文本不在注入语言，防止 IDEA 卡顿
            return
        }

        if (language == JsonLanguage.INSTANCE) {
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
        } else {
            val textRange = InjectionUtils.innerRange(host) ?: return

            registrar.startInjecting(language)
            registrar.addPlace(null, null, host, textRange)
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
