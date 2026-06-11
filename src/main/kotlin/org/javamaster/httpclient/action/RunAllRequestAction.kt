package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.application
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.nls.NlsBundle.nls
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

        val topForm = HttpEditorTopForm.getSelectedEditorTopForm(project) ?: return

        ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.SERVICES)?.show()

        val virtualFile = topForm.file

        application.executeOnPooledThread {
            project.getService(RunHttpFileService::class.java)
                .runRequests(virtualFile) {
                    if (it == 0) {
                        NotifyUtil.notifyInfo(project, nls("run.file.finished", virtualFile.name))
                    }
                }
        }
    }

}
