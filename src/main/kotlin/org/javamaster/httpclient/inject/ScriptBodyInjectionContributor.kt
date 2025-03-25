package org.javamaster.httpclient.inject

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.javamaster.httpclient.psi.HttpScriptBody
import org.javamaster.httpclient.utils.InjectionUtils
import ris58h.webcalm.javascript.JavaScriptLanguage

/**
 * @author yudong
 */
class ScriptBodyInjectionContributor : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val textRange = InjectionUtils.innerRange(context) ?: return

        registrar.startInjecting(JavaScriptLanguage)
        registrar.addPlace(null, null, context as PsiLanguageInjectionHost, textRange)
        registrar.doneInjecting()
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(HttpScriptBody::class.java)
    }

}
