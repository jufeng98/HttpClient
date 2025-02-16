package org.javamaster.httpclient.dashboard

import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.wm.ToolWindowId
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent

class HttpRunContentDescriptor(
    @Nullable executionConsole: HttpExecutionConsole,
    @Nullable processHandler: HttpProcessHandler,
    @NotNull component: JComponent,
    displayName: String,
) : RunContentDescriptor(executionConsole, processHandler, component, displayName) {
    init {
        isActivateToolWindowWhenAdded = false
        isAutoFocusContent = false
        isSelectContentWhenAdded = true
        contentToolWindowId = ToolWindowId.SERVICES
    }
}
