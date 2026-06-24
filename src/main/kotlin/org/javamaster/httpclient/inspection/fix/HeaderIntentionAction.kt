package org.javamaster.httpclient.inspection.fix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpHeaderField

/**
 * @author yudong
 */
class HeaderIntentionAction(private val headerField: HttpHeaderField) : IntentionAction {

    override fun getText(): @IntentionName String {
        return nls("redundant.header")
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return true
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        headerField.delete()
    }

    override fun startInWriteAction(): Boolean {
        return true
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return text
    }

}
