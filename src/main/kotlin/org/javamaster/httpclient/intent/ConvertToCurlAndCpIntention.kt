package org.javamaster.httpclient.intent

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.action.ConvertToCurlAndCpAction
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpRequestBlock

/**
 * @author yudong
 */
class ConvertToCurlAndCpIntention : BaseElementAtCaretIntentionAction() {

    override fun getFamilyName(): String {
        return text
    }

    override fun getText(): String {
        return nls("convert.to.curl.cp")
    }

    override fun isAvailable(project: Project, editor: Editor, psiElement: PsiElement): Boolean {
        return PsiTreeUtil.getParentOfType(psiElement, HttpRequestBlock::class.java) != null
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun invoke(project: Project, editor: Editor, psiElement: PsiElement) {
        val requestBlock = PsiTreeUtil.getParentOfType(psiElement, HttpRequestBlock::class.java) ?: return

        runInEdt {
            ConvertToCurlAndCpAction.convertToCurlAnCy(requestBlock, project, editor)
        }
    }
}
