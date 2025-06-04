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
class HttpRequestBlock(
    node: ASTNode,
    private val mySettings: CodeStyleSettings,
) :
    HttpRequestBaseBlock(node), SettingsAwareBlock {

    override fun createBlock(node: ASTNode): Block {
        val type = node.elementType
        return if (type === HttpTypes.REQUEST_MESSAGES_GROUP) {
            HttpRequestBodyGroupBlock(node, settings)
        } else if (type === HttpTypes.REQUEST_TARGET) {
            HttpRequestTargetBlock(node)
        } else if (type == HttpTypes.RESPONSE_HANDLER && type == HttpTypes.PRE_REQUEST_HANDLER) {
            HttpHandlerBlock(node)
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

    override fun getSettings(): CodeStyleSettings {
        return mySettings
    }
}