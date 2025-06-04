package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.TokenType


abstract class HttpRequestBaseBlock(private val myNode: ASTNode) : ASTBlock {
    private val myIndent: Indent = Indent.getAbsoluteNoneIndent()
    private var myChildren: List<Block>? = null

    protected open val subBlocksInternal: List<Block>
        get() {
            val nodes = node.getChildren(null)
            val blocks = mutableListOf<Block>()

            for (node in nodes) {
                if (node.elementType !== TokenType.WHITE_SPACE) {
                    blocks.add(createBlock(node))
                }
            }

            return blocks
        }

    override fun getSubBlocks(): List<Block> {
        if (myChildren == null) {
            myChildren = subBlocksInternal
        }

        return myChildren!!
    }

    protected open fun createBlock(node: ASTNode): Block {
        return HttpRequestLeafBlock(node)
    }

    override fun getTextRange(): TextRange {
        return myNode.textRange
    }

    override fun getIndent(): Indent? {
        return myIndent
    }

    override fun getAlignment(): Alignment? {
        return null
    }

    override fun isIncomplete(): Boolean {
        return false
    }

    override fun isLeaf(): Boolean {
        return true
    }

    override fun getNode(): ASTNode {
        return myNode
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        return NONE
    }

    companion object {
        private val NONE = ChildAttributes(Indent.getAbsoluteNoneIndent(), null)
    }
}

