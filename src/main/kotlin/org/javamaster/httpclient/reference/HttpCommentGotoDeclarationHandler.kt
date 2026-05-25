package org.javamaster.httpclient.reference

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.util.elementType
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpTypes
import java.io.File

/**
 * @author yudong
 */
class HttpCommentGotoDeclarationHandler : GotoDeclarationHandler {

    override fun getGotoDeclarationTargets(element: PsiElement?, offset: Int, editor: Editor?): Array<PsiElement> {
        if (element == null || element.elementType != HttpTypes.LINE_COMMENT) {
            return arrayOf()
        }

        val project = element.project

        val comment = element.text

        val key1 = "// " + nls("save.to.file", "")
        val psiFile1 = findFileFromComment(key1, comment, offset, project)
        if (psiFile1 != null) {
            return arrayOf(psiFile1)
        }

        val key2 = "// " + nls("cookie.saved", "")
        val psiFile2 = findFileFromComment(key2, comment, offset, project)
        if (psiFile2 != null) {
            return arrayOf(psiFile2)
        }

        return arrayOf()
    }

    private fun findFileFromComment(key: String, comment: String, offset: Int, project: Project): PsiFile? {
        if (offset <= key.length || !comment.startsWith(key)) return null

        val filePath = comment.substring(key.length)
        val virtualFile = VfsUtil.findFileByIoFile(File(filePath), true) ?: return null

        return PsiUtil.getPsiFile(project, virtualFile)
    }

}
