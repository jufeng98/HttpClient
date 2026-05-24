package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.SettingsAwareBlock
import com.intellij.psi.util.lastLeaf
import org.apache.commons.lang3.StringUtils
import org.javamaster.httpclient.psi.HttpTypes
import kotlin.text.isWhitespace

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

        val psiElement1 = child1.node.psi
        val nextSibling = psiElement1.nextSibling
        if (nextSibling is PsiWhiteSpace) {
            val text = nextSibling.text
            val lineFeeds = StringUtils.countMatches(text, "\n")
            return if (lineFeeds == 1) {
                Spacing.createSpacing(0, 0, 2, true, 100)
            } else {
                Spacing.getReadOnlySpacing()
            }
        }

        val lastLeaf = psiElement1.lastLeaf()
        val text = lastLeaf.text
        val length = text.length
        return if (length > 1 && text[length - 1] == '\n' && !text[length - 2].isWhitespace()) {
            Spacing.createSpacing(0, 0, 1, true, 100)
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