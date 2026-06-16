package org.javamaster.httpclient.reference.support

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.impl.FakePsiElement

/**
 * @author yudong
 */
class HttpLineCommentPsiReference(
    psiElement: PsiElement,
    val textRange: TextRange,
    val filePath: String,
) :
    PsiReferenceBase<PsiElement>(psiElement, textRange) {

    override fun resolve(): PsiElement? {
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath) ?: return null

        val project = myElement.project
        val psiFile = virtualFile.findPsiFile(project)
        if (psiFile != null) {
            return psiFile
        }

        return VirtualFileFakePsiElement(virtualFile, project, myElement)
    }

    private class VirtualFileFakePsiElement(
        private val virtualFile: VirtualFile,
        private val project: Project,
        private val myElement: PsiElement,
    ) : FakePsiElement() {

        override fun canNavigate(): Boolean {
            return true
        }

        override fun navigate(requestFocus: Boolean) {
            FileEditorManager.getInstance(project).openFile(virtualFile)
        }

        override fun getName(): String? {
            return virtualFile.name
        }

        override fun getParent(): PsiElement? {
            return myElement
        }
    }
}
