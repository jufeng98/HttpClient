package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.javamaster.httpclient.psi.HttpTypes

/**
 * @author yudong
 */
class HttpRequestBodyGroupBlock(node: ASTNode, private val mySettings: CodeStyleSettings) : HttpRequestBaseBlock(node) {

    override fun createBlock(node: ASTNode): Block {
        val type = node.elementType
        return if (type === HttpTypes.MESSAGE_BODY) {
            HttpRequestBodyBlock(node, mySettings)
        } else {
            super.createBlock(node)
        }
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
