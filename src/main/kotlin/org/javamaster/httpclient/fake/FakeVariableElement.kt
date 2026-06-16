package org.javamaster.httpclient.fake

import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpMessageBody

/**
 * @author yudong
 */
class FakeVariableElement(
    val lineNumber: Int,
    val column: Int,
    val offset: Int,
    private val variableName: String,
    private val httpFile: HttpFile,
    private val messageBody: HttpMessageBody,
) : FakePsiElement() {
    private val textRange = messageBody.textRange

    override fun isValid(): Boolean {
        return messageBody.isValid && messageBody.textRange == textRange
    }

    override fun canNavigate(): Boolean {
        return isValid
    }

    override fun navigate(requestFocus: Boolean) {
        val fileEditorManager = FileEditorManager.getInstance(httpFile.project)

        val editors = fileEditorManager.openFile(httpFile.virtualFile, requestFocus)
        if (editors.isEmpty()) {
            return
        }

        val textEditor = editors[0] as TextEditor
        val editor = textEditor.editor
        editor.caretModel.moveToOffset(offset)

        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
    }

    override fun getParent(): PsiElement {
        return messageBody
    }

    override fun getName(): String {
        return variableName
    }
}
