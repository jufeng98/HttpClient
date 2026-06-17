package org.javamaster.httpclient.fake

import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement

/**
 * @author yudong
 */
class FakeUnsolvedVariableElement(
    val offset: Int,
    private val variableName: String,
    private val file: VirtualFile,
    private val psiElement: PsiElement,
) : FakePsiElement() {
    private val textRange = psiElement.textRange

    override fun isValid(): Boolean {
        return psiElement.isValid && psiElement.textRange == textRange
    }

    override fun canNavigate(): Boolean {
        return isValid
    }

    override fun navigate(requestFocus: Boolean) {
        val fileEditorManager = FileEditorManager.getInstance(psiElement.project)

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
        return psiElement
    }

    override fun getName(): String {
        return variableName
    }
}
