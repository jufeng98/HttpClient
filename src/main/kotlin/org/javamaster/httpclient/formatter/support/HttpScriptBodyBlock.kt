package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings

/**
 * @author yudong
 */
class HttpScriptBodyBlock(node: ASTNode, mySettings: CodeStyleSettings) : HttpRequestBaseBlock(node) {
    private val httpInjectedBlockBuilder = HttpDefaultInjectedLanguageBlockBuilder(mySettings)

    override val subBlocksInternal: List<Block>
        get() {
            val result = mutableListOf<Block>()

            httpInjectedBlockBuilder.addInjectedBlocks(result, node, wrap, alignment, Indent.getAbsoluteNoneIndent())

            return result
        }

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getSpacing(block1: Block?, block2: Block): Spacing? {
        return Spacing.getReadOnlySpacing()
    }

}
