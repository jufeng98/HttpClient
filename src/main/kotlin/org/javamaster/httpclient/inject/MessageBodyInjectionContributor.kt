package org.javamaster.httpclient.inject

import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpMultipartField
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.InjectionUtils

/**
 * @author yudong
 */
class MessageBodyInjectionContributor : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        var language: Language = PlainTextLanguage.INSTANCE
        var textRange: TextRange? = null
        var contentType: ContentType? = null
        if (context is HttpMessageBody) {
            val parent = context.parent
            val parentParent = parent?.parent
            if (parentParent is HttpRequest) {
                contentType = parentParent.contentType
            } else if (parentParent is HttpMultipartField) {
                contentType = parentParent.contentType
            }

            when (contentType) {
                ContentType.APPLICATION_JSON -> {
                    language = JsonLanguage.INSTANCE
                }

                ContentType.APPLICATION_XML, ContentType.TEXT_XML -> {
                    language = XMLLanguage.INSTANCE
                }

                ContentType.TEXT_HTML, ContentType.APPLICATION_XHTML_XML -> {
                    language = HTMLLanguage.INSTANCE
                }
            }

            textRange = InjectionUtils.innerRange(context)
        }

        if (textRange == null) {
            return
        }

        registrar.startInjecting(language)
        registrar.addPlace(null, null, context as PsiLanguageInjectionHost, textRange)
        registrar.doneInjecting()
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(HttpMessageBody::class.java)
    }


}
