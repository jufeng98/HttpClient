package org.javamaster.httpclient.inject

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.util.SmartList
import org.javamaster.httpclient.jsPlugin.support.JavaScript
import org.javamaster.httpclient.psi.HttpScriptBody
import org.javamaster.httpclient.utils.InjectionUtils

/**
 * @author yudong
 */
class ScriptBodyInjectionBackupContributor : MultiHostInjector {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val language = JavaScript.jsLanguage ?: return
        val textRange = InjectionUtils.innerRange(context) ?: return

        registrar.startInjecting(language)
        registrar.addPlace(null, null, context as PsiLanguageInjectionHost, textRange)
        registrar.doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return SmartList(HttpScriptBody::class.java)
    }

}
