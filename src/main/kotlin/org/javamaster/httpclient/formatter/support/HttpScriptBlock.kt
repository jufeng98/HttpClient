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
class HttpScriptBlock(node: ASTNode, val mySettings: CodeStyleSettings) : HttpRequestBaseBlock(node) {

    override fun createBlock(node: ASTNode): Block {
        val type = node.elementType
        return if (type === HttpTypes.SCRIPT_BODY) {
            HttpScriptBodyBlock(node, mySettings)
        } else {
            super.createBlock(node)
        }
    }

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getSpacing(block1: Block?, block2: Block): Spacing? {
        return Spacing.getReadOnlySpacing()
    }

}
