package org.javamaster.httpclient.suppress

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.httpClient.http.request.psi.HttpMessageBody
import com.intellij.httpClient.http.request.psi.HttpScriptBody
import com.intellij.json.JsonLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpLanguage

/**
 * @author yudong
 */
class MyHighlightErrorFilter : HighlightErrorFilter() {
    override fun shouldHighlightErrorElement(element: PsiErrorElement): Boolean {
        val language = element.language
        val project = element.project

        if (language == JsonLanguage.INSTANCE) {
            val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(element)
            return injectionHost !is HttpMessageBody
        }

        if (language is JSLanguageDialect) {
            val httpScriptBody = PsiTreeUtil.getParentOfType(element, HttpScriptBody::class.java)
            return httpScriptBody == null
        }

        return language != HttpLanguage.INSTANCE
    }
}
