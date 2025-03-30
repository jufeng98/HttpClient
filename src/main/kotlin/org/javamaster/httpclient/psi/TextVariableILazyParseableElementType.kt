package org.javamaster.httpclient.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.tree.ILazyParseableElementType
import org.javamaster.httpclient._HttpLexer
import org.javamaster.httpclient.parser.HttpAdapter
import org.javamaster.httpclient.parser.HttpParser
import org.javamaster.httpclient.parser.HttpParserDefinition
import org.javamaster.httpclient.psi.impl.TextVariableLazyFileElement

/**
 * @author yudong
 */
class TextVariableILazyParseableElementType(debugName: String) : ILazyParseableElementType(debugName) {

    override fun parseContents(chameleon: ASTNode): ASTNode {
        val textVariableLazyFileElement = chameleon as TextVariableLazyFileElement

        val httpAdapter = object : HttpAdapter() {
            override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
                super.start(buffer, startOffset, endOffset, _HttpLexer.IN_JSON_VALUE)
            }
        }

        val psiBuilder = PsiBuilderImpl(
            null, null, parserDefinition, httpAdapter,
            null, textVariableLazyFileElement.buffer, null, null
        )

        parseLight(psiBuilder)

        return psiBuilder.treeBuilt
    }

    private fun parseLight(b: PsiBuilder) {
        val t = HttpTypes.MY_JSON_VALUE
        var builder = b
        builder = GeneratedParserUtilBase.adapt_builder_(t, builder, parserDefinition.createParser(null), null)
        val m = GeneratedParserUtilBase.enter_section_(builder, 0, GeneratedParserUtilBase._COLLAPSE_, null)
        val r = parseRoot(builder)
        GeneratedParserUtilBase.exit_section_(builder, 0, m, t, r, true, GeneratedParserUtilBase.TRUE_CONDITION)
    }

    private fun parseRoot(b: PsiBuilder): Boolean {
        return HttpParser.myJsonValue(b, 1)
    }

    companion object {
        private val parserDefinition = HttpParserDefinition()
    }

}
