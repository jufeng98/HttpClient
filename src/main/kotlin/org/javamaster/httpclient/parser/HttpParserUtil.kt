package org.javamaster.httpclient.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase

/**
 * @author yudong
 */
@Suppress("FunctionName", "UNUSED_PARAMETER")
object HttpParserUtil : GeneratedParserUtilBase() {

    @JvmStatic
    fun message_text(psiBuilder: PsiBuilder, level: Int): Boolean {
        val tokenType = psiBuilder.tokenType
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
