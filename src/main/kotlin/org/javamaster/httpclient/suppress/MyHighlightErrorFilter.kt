package org.javamaster.httpclient.suppress

import com.intellij.codeInsight.highlighting.HighlightErrorFilter
import com.intellij.json.JsonLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiErrorElement
import org.javamaster.httpclient.psi.HttpMessageBody

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

        return true
    }
}
