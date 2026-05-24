package org.javamaster.httpclient.formatter.support

import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.DefaultInjectedLanguageBlockBuilder

/**
 * @author yudong
 */
class MyDefaultInjectedLanguageBlockBuilder(settings: CodeStyleSettings) :
    DefaultInjectedLanguageBlockBuilder(settings) {

    override fun createInjectedBlock(
        node: ASTNode,
        originalBlock: Block,
        indent: Indent?,
        offset: Int,
        range: TextRange?,
        language: Language,
    ): Block {
        return MyInjectedLanguageBlockWrapper(originalBlock, offset, range, indent, language)
    }
}