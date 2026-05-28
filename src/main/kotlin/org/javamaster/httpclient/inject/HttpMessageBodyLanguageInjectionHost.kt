package org.javamaster.httpclient.inject

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import org.javamaster.httpclient.factory.HttpPsiFactory

/**
 * Add element injection support
 *
 * @author yudong
 */
open class HttpMessageBodyLanguageInjectionHost(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost {

    override fun isValidHost(): Boolean {
        return true
    }

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val messageBodyNew = HttpPsiFactory.createMessageBody(project, text)
        return this.replace(messageBodyNew) as PsiLanguageInjectionHost
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<PsiLanguageInjectionHost?> {

        return object : LiteralTextEscaper<PsiLanguageInjectionHost?>(this) {

            override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
                outChars.append(rangeInsideHost.substring(myHost!!.text))
                return true
            }

            override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
                return rangeInsideHost.startOffset + offsetInDecoded
            }

            override fun isOneLine(): Boolean {
                return false
            }

        }

    }

}
