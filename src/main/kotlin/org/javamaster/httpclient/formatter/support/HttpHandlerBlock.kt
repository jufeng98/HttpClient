package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode

/**
 * @author yudong
 */
class HttpHandlerBlock(node: ASTNode) : HttpRequestBaseBlock(node) {

    override fun getWrap(): Wrap? {
        return null
    }

    override fun getSpacing(block1: Block?, block2: Block): Spacing? {
        return Spacing.getReadOnlySpacing()
    }

}
