package org.javamaster.httpclient.inject

import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.java.JShellLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.util.elementType
import org.javamaster.httpclient.psi.HttpOrdinaryContent
import org.javamaster.httpclient.psi.HttpScript
import org.javamaster.httpclient.psi.HttpTypes

/**
 * 根据元素类型注入相应的语言
 *
 * @author yudong
 */
class HttpInjectionContributor : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        var language: Language? = null
        var textRange: TextRange? = null
        val elementType = context.firstChild.elementType
        if (elementType == HttpTypes.JSON_TEXT) {
            language = JsonLanguage.INSTANCE
            textRange = innerRange(context)
        } else if (elementType == HttpTypes.XML_TEXT) {
            language = HTMLLanguage.INSTANCE
            textRange = innerRange(context)
        } else if (context is HttpScript) {
            language = JShellLanguage.INSTANCE
            textRange = innerRangeScript(context)
        }

        if (language == null || textRange == null) {
            return
        }

        registrar.startInjecting(language)
        registrar.addPlace(null, null, context as PsiLanguageInjectionHost, textRange)
        registrar.doneInjecting()
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(HttpOrdinaryContent::class.java, HttpScript::class.java)
    }

    private fun innerRange(context: PsiElement): TextRange? {
        val textRange = context.textRange
        val textRangeTmp = textRange.shiftLeft(textRange.startOffset)
        if (textRangeTmp.endOffset == 0) {
            return null
        }

        return textRangeTmp
    }

    private fun innerRangeScript(context: PsiElement): TextRange? {
        var textRange = context.textRange
        textRange = textRange.shiftLeft(textRange.startOffset)
        if (textRange.endOffset == 0) {
            return null
        }

        val textRangeTmp = TextRange(textRange.startOffset + 4, textRange.endOffset - 2)
        if (textRangeTmp.endOffset == 0) {
            return null
        }

        return textRangeTmp
    }
}
