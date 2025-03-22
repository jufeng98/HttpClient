package org.javamaster.httpclient.dashboard

import com.intellij.execution.ui.ExecutionConsole
import javax.swing.JComponent

class HttpExecutionConsole(private val myComponent: JComponent) : ExecutionConsole {
    override fun dispose() {

    }

    override fun getComponent(): JComponent {
        return myComponent
    }

    override fun getPreferredFocusableComponent(): JComponent {
        return myComponent
    }
}
