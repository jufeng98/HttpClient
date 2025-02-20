package org.javamaster.httpclient.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager

/**
 * 通知弹框
 *
 * @author yudong
 */
object NotifyUtil {

    fun notifyWarn(project: Project, message: String?) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindowManager.notifyByBalloon(
            ToolWindowId.SERVICES, MessageType.WARNING,
            "<div style='font-size:18pt'>${message}</div>"
        )
    }

    fun notifyError(project: Project, message: String?) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindowManager.notifyByBalloon(
            ToolWindowId.SERVICES, MessageType.ERROR,
            "<div style='font-size:18pt'>${message}</div>"
        )
    }

}
