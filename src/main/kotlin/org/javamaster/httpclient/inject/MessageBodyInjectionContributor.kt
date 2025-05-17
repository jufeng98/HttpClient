package org.javamaster.httpclient.inject

import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.PsiUtil
import com.intellij.util.SmartList
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.action.dashboard.view.ContentTypeActionGroup
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpMultipartField
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.InjectionUtils

/**
 * @author yudong
 */
class MessageBodyInjectionContributor : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val virtualFile = PsiUtil.getVirtualFile(context)
        if (virtualFile != null) {
            val contentType = virtualFile.getUserData(ContentTypeActionGroup.httpDashboardContentTypeKey)
            if (contentType != null) {
                tryInject(contentType, context, registrar)
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

        tryInject(contentType, context, registrar)
    }

    private fun tryInject(contentType: ContentType?, context: PsiElement, registrar: MultiHostRegistrar) {
        var language: Language = PlainTextLanguage.INSTANCE

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

        val textRange = InjectionUtils.innerRange(context) ?: return

        registrar.startInjecting(language)
        registrar.addPlace(null, null, context as PsiLanguageInjectionHost, textRange)
        registrar.doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return SmartList(HttpMessageBody::class.java)
    }

}
