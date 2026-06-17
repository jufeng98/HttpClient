package org.javamaster.httpclient.fake

import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpHeader

/**
 * @author yudong
 */
class FakeHeaderVariableElement(
    val offset: Int,
    private val variableName: String,
    private val httpFile: HttpFile,
    private val header: HttpHeader,
) : FakePsiElement() {
    private val textRange = header.textRange

    override fun isValid(): Boolean {
        return header.isValid && header.textRange == textRange
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
        return header
    }

    override fun getName(): String {
        return variableName
    }
}
