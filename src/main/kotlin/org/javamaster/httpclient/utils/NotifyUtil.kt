package org.javamaster.httpclient.utils

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/**
 * 通知弹框
 *
 * @author yudong
 */
object NotifyUtil {
    private val STICKY_BALLOON_GROUP: NotificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup("HttpClient.STICKY_BALLOON")

    fun notifyError(project: Project?, message: String?) {
        notify(project, "温馨提示", message)
    }

    fun notifyError(project: Project?, title: String, message: String?) {
        notify(project, title, message)
    }

    private fun notify(
        project: Project?,
        title: String, message: String?,
    ) {
        if (project == null) {
            return
        }

        STICKY_BALLOON_GROUP.createNotification(title, message ?: "null", NotificationType.ERROR).notify(project)
    }
}
