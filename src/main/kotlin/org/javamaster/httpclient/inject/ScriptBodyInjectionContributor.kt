package org.javamaster.httpclient.inject

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.util.SmartList
import org.javamaster.httpclient.jsPlugin.support.WebCalm
import org.javamaster.httpclient.psi.HttpScriptBody
import org.javamaster.httpclient.utils.InjectionUtils
import ris58h.webcalm.javascript.JavaScriptLanguage

/**
 * @author yudong
 */
class ScriptBodyInjectionContributor : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!WebCalm.isAvailable()) {
            return
        }

        val textRange = InjectionUtils.innerRange(context) ?: return

        try {
            registrar.startInjecting(JavaScriptLanguage)
            registrar.addPlace(null, null, context as PsiLanguageInjectionHost, textRange)
            registrar.doneInjecting()
        } catch (e: Error) {
            println(e.message)
        }

    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return SmartList(HttpScriptBody::class.java)
    }

}
