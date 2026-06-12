package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.application
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.action.ChooseEnvironmentAction.Companion.isChooseEnvBeforeRun
import org.javamaster.httpclient.consts.HttpConsts.Companion.SUCCESS
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.service.ChooseEnvBeforeRunService
import org.javamaster.httpclient.service.RunHttpFileService
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class RunAllRequestAction : AnAction(nls("run.all.tooltip"), null, HttpIcons.RUN_ALL) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val editorTopForm = HttpEditorTopForm.getSelectedEditorTopForm(project) ?: return

        val virtualFile = editorTopForm.file

        if (isChooseEnvBeforeRun(editorTopForm.selectedEnv)) {
            project.getService(ChooseEnvBeforeRunService::class.java)
                .createEnvChoosePopup(virtualFile) {
                    executeFileRequests(virtualFile, project, it)
                }
                .showUnderneathOf(e.inputEvent!!.component)
        } else {
            executeFileRequests(virtualFile, project, null)
        }
    }

    private fun executeFileRequests(virtualFile: VirtualFile, project: Project, targetEnv: String?) {
        ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.SERVICES)?.show()

        application.executeOnPooledThread {
            project.getService(RunHttpFileService::class.java)
                .runRequests(virtualFile, targetEnv) {
                    if (it == SUCCESS) {
                        NotifyUtil.notifyInfo(project, nls("run.file.finished", virtualFile.name))
                    } else {
                        NotifyUtil.notifyError(project, nls("run.file.error", virtualFile.name))
                    }
                }
        }
    }
}
