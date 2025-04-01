package org.javamaster.httpclient.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Notification pop-up
 *
 * @author yudong
 */
object NotifyUtil {
    fun notifyInfo(project: Project, message: String?) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindowManager.notifyByBalloon(
            ToolWindowId.SERVICES, MessageType.INFO,
            "<div style='font-size:13pt'>${message}</div>"
        )
    }

    fun notifyWarn(project: Project, message: String?) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindowManager.notifyByBalloon(
            ToolWindowId.SERVICES, MessageType.WARNING,
            "<div style='font-size:13pt'>${message}</div>"
        )
    }

    fun notifyError(project: Project, message: String?) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindowManager.notifyByBalloon(
            ToolWindowId.SERVICES, MessageType.ERROR,
            "<div style='font-size:13pt'>${message}</div>"
        )
    }

}
