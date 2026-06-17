package org.javamaster.httpclient.fake

import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import org.javamaster.httpclient.psi.HttpInputFile

/**
 * @author yudong
 */
class FakeInputFileElement(
    val offset: Int,
    private val variableName: String,
    private val file: VirtualFile,
    private val inputFile: HttpInputFile,
) : FakePsiElement() {
    private val textRange = inputFile.textRange

    override fun isValid(): Boolean {
        return inputFile.isValid && inputFile.textRange == textRange
    }

    override fun canNavigate(): Boolean {
        return isValid
    }

    override fun navigate(requestFocus: Boolean) {
        val fileEditorManager = FileEditorManager.getInstance(inputFile.project)

        val editors = fileEditorManager.openFile(file, requestFocus)
        if (editors.isEmpty()) {
            return
        }

        val textEditor = editors[0] as TextEditor
        val editor = textEditor.editor
        editor.caretModel.moveToOffset(offset)

        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
    }

    override fun getParent(): PsiElement {
        return inputFile
    }

    override fun getName(): String {
        return variableName
    }
}
