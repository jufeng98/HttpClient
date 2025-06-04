package org.javamaster.httpclient.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.SettingsAwareBlock
import org.javamaster.httpclient.formatter.support.HttpDirectionCommentBlock
import org.javamaster.httpclient.formatter.support.HttpRequestBaseBlock
import org.javamaster.httpclient.formatter.support.HttpRequestGroupBlock
import org.javamaster.httpclient.psi.HttpTypes


/**
 * @author yudong
 */
class HttpRequestFileBlock(fileNode: ASTNode, private val mySettings: CodeStyleSettings) :
    HttpRequestBaseBlock(fileNode),
    SettingsAwareBlock {

    override fun createBlock(node: ASTNode): Block {
        return if (node.elementType === HttpTypes.REQUEST_BLOCK) {
            HttpRequestGroupBlock(node, settings)
        } else if (node.elementType == HttpTypes.DIRECTION_COMMENT) {
            HttpDirectionCommentBlock(node, settings)
        } else {
            super.createBlock(node)
        }
    }

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return if (child1 is HttpRequestGroupBlock && child2 is HttpRequestGroupBlock) {
            Spacing.createSpacing(0, 0, 2, true, 100)
        } else if (child1 is HttpDirectionCommentBlock && child2 is HttpRequestGroupBlock) {
            Spacing.createSpacing(0, 0, 2, true, 100)
        } else {
            Spacing.getReadOnlySpacing()
        }
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun getSettings(): CodeStyleSettings {
        return mySettings
    }

}
