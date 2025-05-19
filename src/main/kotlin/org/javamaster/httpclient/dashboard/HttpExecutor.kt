package org.javamaster.httpclient.dashboard

import com.intellij.execution.ExecutionBundle
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

    override fun getRerunIcon(): Icon {
        return AllIcons.Actions.Rerun
    }

    override fun getDisabledIcon(): Icon {
        return IconLoader.getDisabledIcon(icon)
    }

    override fun getDescription(): String {
        return "Run selected configuration"
    }

    override fun getActionName(): String {
        return "Run"
    }

    override fun getId(): String {
        return HTTP_EXECUTOR_ID
    }

    override fun getStartActionText(): String {
        return ExecutionBundle.message("default.runner.start.action.text")
    }

    override fun getContextActionId(): String {
        return "RunRequest"
    }

    override fun getHelpId(): String {
        return "ideaInterface.run"
    }

    companion object {
        const val HTTP_EXECUTOR_ID = "httpExecutor"
    }
}