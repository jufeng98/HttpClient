package org.javamaster.httpclient.dashboard

import com.intellij.execution.Executor
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindowId
import javax.swing.Icon

class HttpExecutor : Executor() {
    override fun getToolWindowId(): String {
        return ToolWindowId.SERVICES
    }

    override fun getToolWindowIcon(): Icon {
        return AllIcons.Toolwindows.ToolWindowRun
    }

    override fun getIcon(): Icon {
        return AllIcons.Actions.Execute
    }

    override fun getDisabledIcon(): Icon {
        return IconLoader.getDisabledIcon(icon)
    }

    override fun getDescription(): String {
        return "Run select request"
    }

    override fun getActionName(): String {
        return "Run Request"
    }

    override fun getId(): String {
        return HTTP_EXECUTOR_ID
    }

    override fun getStartActionText(): String {
        return "Run Request"
    }

    override fun getContextActionId(): String {
        return "RunRequest"
    }

    override fun getHelpId(): String? {
        return null
    }

    companion object {
        const val HTTP_EXECUTOR_ID = "httpExecutor"
    }
}