package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.SettingsAwareBlock
import org.javamaster.httpclient.psi.HttpTypes

/**
 * @author yudong
 */
class HttpRequestGroupBlock(
    fileNode: ASTNode,
    private val mySettings: CodeStyleSettings
) :
    HttpRequestBaseBlock(fileNode), SettingsAwareBlock {

    override fun createBlock(node: ASTNode): Block {
        return if (node.elementType === HttpTypes.REQUEST) {
            HttpRequestBlock(node, settings)
        } else {
            super.createBlock(node)
        }
    }

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return Spacing.getReadOnlySpacing()
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun getSettings(): CodeStyleSettings {
        return mySettings
    }
}

