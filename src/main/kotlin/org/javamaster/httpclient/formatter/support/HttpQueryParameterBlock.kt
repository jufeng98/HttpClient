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
class HttpQueryParameterBlock(node: ASTNode, private val mySettings: CodeStyleSettings) :
    HttpRequestBaseBlock(node),
    SettingsAwareBlock {

    override fun createBlock(node: ASTNode): Block {
        val type = node.elementType
        return if (type === HttpTypes.QUERY_PARAMETER_KEY) {
            HttpQueryParameterKeyBlock(node, mySettings)
        } else if (type == HttpTypes.EQUALS) {
            HttpEqualsBlock(node, mySettings)
        } else if (type == HttpTypes.QUERY_PARAMETER_VALUE) {
            HttpQueryParameterValueBlock(node, mySettings)
        } else {
            super.createBlock(node)
        }
    }

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        if (child1 is HttpQueryParameterKeyBlock && child2 is HttpEqualsBlock) {
            return Spacing.createSpacing(1, 1, 0, false, 0)
        }

        if (child1 is HttpEqualsBlock && child2 is HttpQueryParameterValueBlock) {
            return Spacing.createSpacing(1, 1, 0, false, 0)
        }

        return Spacing.getReadOnlySpacing()
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun getSettings(): CodeStyleSettings {
        return mySettings
    }

}
