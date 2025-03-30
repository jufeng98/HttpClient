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
import org.javamaster.httpclient.psi.impl.UrlEncodedLazyFileElement

/**
 * @author yudong
 */
class UrlEncodedLazyParseableElementType(debugName: String) : ILazyParseableElementType(debugName) {

    override fun parseContents(chameleon: ASTNode): ASTNode {
        val urlEncodedLazyFileElement = chameleon as UrlEncodedLazyFileElement

        val httpAdapter = object : HttpAdapter() {
            override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
                (this.flex as _HttpLexer).nameFlag = true
                super.start(buffer, startOffset, endOffset, _HttpLexer.IN_QUERY)
            }
        }

        val psiBuilder = PsiBuilderImpl(
            null, null, parserDefinition, httpAdapter,
            null, urlEncodedLazyFileElement.buffer, null, null
        )

        parseLight(psiBuilder)

        return psiBuilder.treeBuilt
    }

    private fun parseLight(b: PsiBuilder) {
        var builder = b
        val t = MyHttpTypes.URL_ENCODED_FILE
        builder = GeneratedParserUtilBase.adapt_builder_(t, builder, parserDefinition.createParser(null), null)
        val m = GeneratedParserUtilBase.enter_section_(builder, 0, GeneratedParserUtilBase._COLLAPSE_, null)
        val r = parseRoot(builder)
        GeneratedParserUtilBase.exit_section_(builder, 0, m, t, r, true, GeneratedParserUtilBase.TRUE_CONDITION)
    }

    private fun parseRoot(b: PsiBuilder): Boolean {
        return HttpParser.query(b, 1)
    }

    companion object {
        private val parserDefinition = HttpParserDefinition()
    }

}
