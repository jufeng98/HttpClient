package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiUtil

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

        return PsiUtil.getPsiFile(myElement.project, virtualFile)
    }

}
