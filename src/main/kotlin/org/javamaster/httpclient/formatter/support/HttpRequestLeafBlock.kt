package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode

class HttpRequestLeafBlock @JvmOverloads constructor(node: ASTNode, private val myWithIndent: Boolean = false) :
    HttpRequestBaseBlock(node) {
    override val subBlocksInternal: List<Block>
        get() = emptyList()

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getIndent(): Indent? {
        return if (this.myWithIndent) Indent.getNoneIndent() else super.getIndent()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return null
    }
}
