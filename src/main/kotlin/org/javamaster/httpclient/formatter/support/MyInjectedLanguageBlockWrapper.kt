package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.formatter.common.InjectedLanguageBlockWrapper

/**
 * @author yudong
 */
class MyInjectedLanguageBlockWrapper(
    original: Block,
    offset: Int,
    range: TextRange?,
    indent: Indent?,
    language: Language?,
) : InjectedLanguageBlockWrapper(original, offset, range, indent, language) {

    override fun getSpacing(child1: Block?, child2: Block): Spacing {
        return Spacing.getReadOnlySpacing()
    }

}
