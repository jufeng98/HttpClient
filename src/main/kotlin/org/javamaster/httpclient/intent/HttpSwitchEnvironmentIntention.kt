package org.javamaster.httpclient.intent

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.ui.HttpEditorTopForm

/**
 * @author yudong
 */
class HttpSwitchEnvironmentIntention : BaseIntentionAction() {

    override fun getFamilyName(): String {
        return text
    }

    override fun getText(): String {
        return NlsBundle.nls("switch.environment")
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return file is HttpFile
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val topForm = HttpEditorTopForm.getSelectedEditorTopForm(project) ?: return

        val presetEnvSet = topForm.chooseEnvironmentAction.presetEnvSet ?: return

        editor?.virtualFile ?: return

        val popupFactory = JBPopupFactory.getInstance()
        popupFactory.createPopupChooserBuilder(presetEnvSet.toList())
            .setItemChosenCallback { item -> topForm.setSelectEnv(item) }
            .createPopup()
            .showInBestPositionFor(editor)
    }
}
