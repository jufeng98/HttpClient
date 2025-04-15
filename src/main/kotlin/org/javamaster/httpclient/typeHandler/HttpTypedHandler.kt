package org.javamaster.httpclient.typeHandler

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpHeaderField
import org.javamaster.httpclient.psi.HttpRequestBlock


class HttpTypedHandler : TypedHandlerDelegate() {
    override fun beforeCharTyped(
        c: Char, project: Project,
        editor: Editor, file: PsiFile,
        fileType: FileType,
    ): Result {
        editor.virtualFile ?: return Result.CONTINUE
        val psiFile = PsiUtil.getPsiFile(project, editor.virtualFile)

        if (psiFile !is HttpFile || c != '%' && c != '{') {
            return Result.CONTINUE
        }

        val offset = editor.caretModel.offset
        val elementAtOffset = file.findElementAt(offset - 1) ?: return Result.CONTINUE

        val document = editor.document
        val charBefore = getCharAt(document, offset - 1)
        val charAfter = getCharAt(document, offset)
        if (charBefore == '{') {
            if (c == '%' && charAfter != '%') {
                return this.addBrace(project, editor, document, "%\n    \n%}", 6)
            }

            if (c == '{' && charAfter != '}') {
                return this.addBrace(project, editor, document, "{}}", 1)
            }
        } else if (c == '{' && charAfter != '{' && couldCompleteToMessageBody(elementAtOffset, document, offset)) {
            return this.addBrace(project, editor, document, "{}", 1)
        }

        return Result.CONTINUE
    }

    private fun addBrace(project: Project, editor: Editor, document: Document, s: String, caretShift: Int): Result {
        val documentManager = PsiDocumentManager.getInstance(project)
        if (documentManager != null) {
            EditorModificationUtil.insertStringAtCaret(editor, s, true, caretShift)
            documentManager.commitDocument(document)
            return Result.STOP
        } else {
            return Result.CONTINUE
        }
    }

    private fun getCharAt(document: Document, offset: Int): Char {
        return if (offset < document.textLength && offset >= 0) document.charsSequence[offset] else '\u0000'
    }

    private fun couldCompleteToMessageBody(element: PsiElement, document: Document, offset: Int): Boolean {
        if (element !is PsiWhiteSpace) {
            return false
        }

        val sibling = element.getPrevSibling()
        if (sibling is HttpRequestBlock) {
            val request = sibling.request
            if (request.header?.headerFieldList.isNullOrEmpty()) {
                return false
            }
        } else if (sibling !is HttpHeaderField) {
            return false
        }

        var prevOffset = offset - 1
        var countOfLineBreaks = 0
        while (StringUtil.isWhiteSpace(getCharAt(document, prevOffset))) {
            if (StringUtil.isLineBreak(getCharAt(document, prevOffset))) {
                ++countOfLineBreaks
            }
            --prevOffset
        }

        return countOfLineBreaks > 1
    }
}
