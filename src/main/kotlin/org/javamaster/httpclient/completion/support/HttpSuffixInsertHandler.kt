package org.javamaster.httpclient.completion.support

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorModificationUtilEx
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager

class HttpSuffixInsertHandler(private val mySuffix: String) : InsertHandler<LookupElement> {
    private val myShortSuffix = mySuffix.trim { it <= ' ' }

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val project = context.project
        val editor = context.editor
        val document = editor.document

        // When LookupElementBuilder Set the caseSensitive property to true, For some reason,
        // it becomes lowercase after completion. Here, we force correction
        val text = item.lookupString
        document.replaceString(context.startOffset, context.tailOffset, text)

        val documentManager = PsiDocumentManager.getInstance(project)
        documentManager.commitDocument(editor.document)

        val offset = StringUtil.skipWhitespaceForward(document.charsSequence, editor.caretModel.offset)
        if (document.textLength == offset || !isEqualsToSuffix(document, offset)) {
            EditorModificationUtilEx.insertStringAtCaret(editor, mySuffix)
            documentManager.commitDocument(editor.document)
        }

        editor.caretModel.moveToOffset(offset + mySuffix.length)
    }

    private fun isEqualsToSuffix(document: Document, offset: Int): Boolean {
        val endOffset = offset + myShortSuffix.length - 1
        return document.textLength > endOffset && StringUtil.equals(
            this.myShortSuffix,
            document.charsSequence.subSequence(offset, endOffset + 1).toString()
        )
    }

    override fun toString(): String {
        return this.mySuffix
    }

    companion object {
        val FIELD_SEPARATOR: HttpSuffixInsertHandler = HttpSuffixInsertHandler(": ")
    }
}
