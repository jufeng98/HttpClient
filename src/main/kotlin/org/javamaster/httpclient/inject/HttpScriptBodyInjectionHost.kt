package org.javamaster.httpclient.inject

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost

/**
 * Add element injection support
 *
 * @author yudong
 */
open class HttpScriptBodyInjectionHost(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost {

    override fun isValidHost(): Boolean {
        return true
    }

    override fun updateText(text: String): PsiLanguageInjectionHost {
        return ElementManipulators.handleContentChange(this, text) as PsiLanguageInjectionHost
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<PsiLanguageInjectionHost?> {
        return LiteralTextEscaper.createSimple<PsiLanguageInjectionHost?>(this as PsiLanguageInjectionHost)
    }

}
