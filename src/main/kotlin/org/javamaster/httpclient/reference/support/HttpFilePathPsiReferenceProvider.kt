package org.javamaster.httpclient.reference.support

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.utils.HttpUtils
import java.io.File

class HttpFilePathPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val textRange = element.textRange
        val httpFilePath = element as HttpFilePath
        val rangeInElement = textRange.shiftLeft(textRange.startOffset)

        val psiReference = object : PsiReferenceBase<HttpFilePath>(httpFilePath, rangeInElement) {

            override fun resolve(): PsiElement {
                return HttpFilePathFakePsiElement(httpFilePath)
            }

        }

        return arrayOf(psiReference)
    }

    class HttpFilePathFakePsiElement(val element: HttpFilePath) : ASTWrapperPsiElement(element.node) {

        override fun navigate(requestFocus: Boolean) {
            val text = element.text
            val project = element.project
            val httpFile = element.containingFile
            val parentPath = httpFile.virtualFile.parent.path

            val path = HttpUtils.constructFilePath(text, parentPath, httpFile)

            val file = File(path)
            if (!file.exists()) {
                HttpFakePsiElement.showTip("文件:${file.absoluteFile.normalize().absolutePath}不存在!", project)
                return
            }

            val virtualFile = VfsUtil.findFileByIoFile(file, true)!!

            val openFile = FileEditorManager.getInstance(project).openFile(virtualFile, true)

            if (openFile.isEmpty()) {
                // 无法打开文件,就跳转到其目录
                ProjectView.getInstance(project).select(null, virtualFile, true)
            }
        }

    }

}
