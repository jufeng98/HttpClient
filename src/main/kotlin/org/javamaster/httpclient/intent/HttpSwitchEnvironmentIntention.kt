package org.javamaster.httpclient.intent

import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.ui.popup.PopupFactoryImpl
import org.javamaster.httpclient.env.EnvFileService.Companion.getService
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

        val envFileService = getService(project)
        val path = file?.virtualFile?.parent?.path ?: return

        val envSet = envFileService.getPresetEnvSet(path)

        val popupFactory = PopupFactoryImpl.getInstance()
        popupFactory.createPopupChooserBuilder(envSet.toList())
            .setItemChosenCallback { item -> topForm.setSelectEnv(item) }
            .createPopup()
            .showInBestPositionFor(editor!!)
    }
}
