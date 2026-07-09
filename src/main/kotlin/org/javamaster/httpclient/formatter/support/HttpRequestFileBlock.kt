package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.SettingsAwareBlock
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.javamaster.httpclient.psi.HttpTypes

/**
 * @author yudong
 */
class HttpRequestFileBlock(fileNode: ASTNode, private val mySettings: CodeStyleSettings) :
    HttpRequestBaseBlock(fileNode),
    SettingsAwareBlock {

    override fun createBlock(node: ASTNode): Block {
        return if (node.elementType === HttpTypes.REQUEST_BLOCK) {
            HttpRequestBlockBlock(node, settings)
        } else if (node.elementType == HttpTypes.GLOBAL_HANDLER) {
            HttpHandlerBlock(node, settings)
        } else {
            super.createBlock(node)
        }
    }

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        if (child1 !is HttpRequestBlockBlock || child2 !is HttpRequestBlockBlock) {
            return Spacing.getReadOnlySpacing()
        }

        val element = child1.node.psi
        val lastChild = PsiTreeUtil.lastChild(element)
        if (lastChild.elementType == HttpTypes.MESSAGE_BOUNDARY && lastChild.text.contains("--\n")) {
            return Spacing.getReadOnlySpacing()
        }

        return Spacing.createSpacing(0, 0, 2, false, 1)
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun getSettings(): CodeStyleSettings {
        return mySettings
    }

}