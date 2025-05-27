package org.javamaster.httpclient.liveTemplates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.psi.HttpScriptBody


/**
 * @author yudong
 */
class HttpJsHandlerTemplateContextType : TemplateContextType("Http js handler") {
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        return inContext(templateActionContext.file, templateActionContext.startOffset)
    }

    private fun inContext(file: PsiFile, offset: Int): Boolean {
        val element = file.findElementAt(offset) ?: return false

        val injectedLanguageManager = InjectedLanguageManager.getInstance(file.project)

        val injectionHost = injectedLanguageManager.getInjectionHost(element)

        return injectionHost is HttpScriptBody
    }
}
