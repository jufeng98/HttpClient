package org.javamaster.httpclient.utils

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
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
    private val STICKY_STICKY_BALLOON by lazy {
        NotificationGroupManager.getInstance().getNotificationGroup("HttpClient.STICKY_BALLOON")
    }
    private const val TOOL_WINDOW_ID = ToolWindowId.SERVICES

    fun notifyInfo(project: Project, message: String?) {
        notifyServicesBalloon(project, message, MessageType.INFO)
    }

    fun notifyWarn(project: Project, message: String?) {
        notifyServicesBalloon(project, message, MessageType.WARNING)
    }

    fun notifyError(project: Project, message: String?) {
        notifyServicesBalloon(project, message, MessageType.ERROR)
    }

    private fun notifyServicesBalloon(project: Project, message: String?, type: MessageType) {
        val toolWindowManager = ToolWindowManager.getInstance(project)

        toolWindowManager.notifyByBalloon(TOOL_WINDOW_ID, type, "<div style='font-size:13pt'>${message}</div>")
    }

    fun notifyCornerSuccess(project: Project, message: String) {
        STICKY_STICKY_BALLOON.createNotification("Tip", message, NotificationType.INFORMATION).notify(project)
    }

    fun notifyCornerWarn(project: Project, message: String) {
        STICKY_STICKY_BALLOON.createNotification("Tip", message, NotificationType.WARNING).notify(project)
    }

    fun notifyCornerError(project: Project, message: String) {
        STICKY_STICKY_BALLOON.createNotification("Tip", message, NotificationType.ERROR).notify(project)
    }

}
