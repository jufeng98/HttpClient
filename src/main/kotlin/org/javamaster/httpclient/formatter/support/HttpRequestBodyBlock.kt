package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.DefaultInjectedLanguageBlockBuilder

/**
 * @author yudong
 */
class HttpRequestBodyBlock(node: ASTNode, settings: CodeStyleSettings) : HttpRequestBaseBlock(node) {
    private val myInjectedBlockBuilder = DefaultInjectedLanguageBlockBuilder(settings)

    override val subBlocksInternal: List<Block>
        get() {
            val result = mutableListOf<Block>()

            myInjectedBlockBuilder.addInjectedBlocks(
                result,
                node,
                wrap,
                alignment,
                Indent.getAbsoluteNoneIndent()
            )
            return result
        }

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return null
    }

    override fun isLeaf(): Boolean {
        return false
    }
}
