package org.javamaster.httpclient.dashboard

import com.intellij.execution.ui.ExecutionConsole
import javax.swing.JComponent

class HttpExecutionConsole(val component1: JComponent) : ExecutionConsole {
    override fun dispose() {

    }

    override fun getComponent(): JComponent {
        return component1
    }

    override fun getPreferredFocusableComponent(): JComponent {
        return component1
    }
}
