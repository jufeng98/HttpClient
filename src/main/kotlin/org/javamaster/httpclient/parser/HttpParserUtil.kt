package org.javamaster.httpclient.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import org.javamaster.httpclient.psi.HttpTypes

/**
 * @author yudong
 */
@Suppress("FunctionName", "UNUSED_PARAMETER")
object HttpParserUtil : GeneratedParserUtilBase() {

    @JvmStatic
    fun message_text(psiBuilder: PsiBuilder, level: Int): Boolean {
        val tokenType = psiBuilder.tokenType
        if (tokenType == HttpTypes.MESSAGE_BOUNDARY) {
            return false
        }

        consumeToken(psiBuilder, tokenType)
        return true
    }

    @JvmStatic
    fun script_body(psiBuilder: PsiBuilder, level: Int): Boolean {
        val tokenType = psiBuilder.tokenType
        consumeToken(psiBuilder, tokenType)
        return true
    }

}
