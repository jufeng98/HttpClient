package org.javamaster.httpclient.formatter

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.SettingsAwareBlock
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.formatter.support.HttpHandlerBlock
import org.javamaster.httpclient.formatter.support.HttpRequestBaseBlock
import org.javamaster.httpclient.formatter.support.HttpRequestBlockBlock
import org.javamaster.httpclient.psi.HttpResponseHandler
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

        val handler = PsiTreeUtil.findChildOfType(child1.node.psi, HttpResponseHandler::class.java)
        if (handler != null) {
            val text = handler.text
            val length = text.length
            return if (text[length - 1] == '\n' && !text[length - 2].isWhitespace()) {
                Spacing.createSpacing(0, 0, 1, true, 100)
            } else {
                Spacing.getReadOnlySpacing()
            }
        }

        return Spacing.createSpacing(0, 0, 2, true, 100)
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun getSettings(): CodeStyleSettings {
        return mySettings
    }

}
