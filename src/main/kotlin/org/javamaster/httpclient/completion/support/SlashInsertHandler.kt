package org.javamaster.httpclient.completion.support

import com.intellij.codeInsight.completion.BasicInsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

/**
 * @author yudong
 */
object SlashInsertHandler : BasicInsertHandler<LookupElement>() {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val editor = context.editor
        val document = editor.document

        context.commitDocument()

        document.insertString(context.tailOffset, "/")
        editor.caretModel.moveToOffset(context.tailOffset)
    }

}

object SlashEndInsertHandler : BasicInsertHandler<LookupElement>() {

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val editor = context.editor
        val document = editor.document

        context.commitDocument()

        val offset = context.tailOffset + 2

        document.insertString(offset, "/")

        editor.caretModel.moveToOffset(offset + 1)
    }

}
