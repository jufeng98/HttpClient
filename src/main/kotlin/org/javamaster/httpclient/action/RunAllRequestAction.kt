package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.handler.RunFileHandler.runRequests
import org.javamaster.httpclient.handler.RunFileHandler.stopRunning
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.ui.HttpEditorTopForm

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class RunAllRequestAction : AnAction(nls("run.all.tooltip"), null, HttpIcons.RUN_ALL) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!
        val presentation = e.presentation
        val topForm = HttpEditorTopForm.getSelectedEditorTopForm(project)!!

        if (presentation.icon === HttpIcons.STOP) {
            switchRunBtnToInitialing(presentation)

            stopRunning()
        } else {
            switchRunBtnToStopping(presentation)

            runRequests(project, topForm) {
                switchRunBtnToInitialing(presentation)
            }
        }
    }

    private fun switchRunBtnToInitialing(presentation: Presentation) {
        presentation.description = nls("run.all.tooltip")
        presentation.icon = HttpIcons.RUN_ALL
    }

    private fun switchRunBtnToStopping(presentation: Presentation) {
        presentation.description = nls("stop.running")
        presentation.icon = HttpIcons.STOP
    }


}
