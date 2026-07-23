package org.javamaster.httpclient.intent

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.IntentUtil
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.SensitiveDataUtil

/**
 * @author yudong
 */
class ClearCertPwdIntention : BaseIntentionAction() {

    override fun getFamilyName(): String {
        return text
    }

    override fun getText(): String {
        return NlsBundle.nls("cert.pwd.clear.hint")
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return IntentUtil.checkHasCertificatePassphrase(editor, file)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return
        }

        SensitiveDataUtil.save(HttpConsts.CERT_PWD, null)

        NotifyUtil.notifyCornerSuccess(project, "证书密码已清除")
    }
}
