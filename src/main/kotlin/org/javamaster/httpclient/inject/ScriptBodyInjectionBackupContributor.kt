package org.javamaster.httpclient.inject

import com.intellij.ide.plugins.PluginManager
import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpScriptBody
import org.javamaster.httpclient.utils.InjectionUtils

/**
 * @author yudong
 */
class ScriptBodyInjectionBackupContributor : MultiHostInjector {
    private val jsLanguage: Language? by lazy {
        val pluginId = PluginId.findId("JavaScript") ?: return@lazy null
        val plugin = PluginManager.getInstance().findEnabledPlugin(pluginId) ?: return@lazy null
        val pluginClassLoader = plugin.pluginClassLoader ?: return@lazy null

        val name = "com.intellij.lang.javascript.JavaScriptSupportLoader"
        val clz = pluginClassLoader.loadClass(name)

        val field = clz.getDeclaredField("JAVASCRIPT")
        field.isAccessible = true
        val languageFileType = field.get(null) as LanguageFileType

        languageFileType.language
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val language = jsLanguage ?: return
        val textRange = InjectionUtils.innerRange(context) ?: return

        registrar.startInjecting(language)
        registrar.addPlace(null, null, context as PsiLanguageInjectionHost, textRange)
        registrar.doneInjecting()
    }

    override fun elementsToInjectIn(): MutableList<out Class<out PsiElement>> {
        return mutableListOf(HttpMessageBody::class.java, HttpScriptBody::class.java)
    }

}
