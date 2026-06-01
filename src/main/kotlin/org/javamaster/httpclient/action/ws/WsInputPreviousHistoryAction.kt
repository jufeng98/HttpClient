package org.javamaster.httpclient.action.ws

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.ui.HttpDashboardForm

/**
 * @author yudong
 */
class WsInputPreviousHistoryAction(private val wsDashboardForm: HttpDashboardForm.WsDashboardForm) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        wsDashboardForm.switchWsToPreviousInput()
    }

}
