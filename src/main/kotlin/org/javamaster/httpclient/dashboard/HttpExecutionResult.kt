package org.javamaster.httpclient.dashboard

import com.intellij.execution.ExecutionResult
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.openapi.actionSystem.AnAction
import org.javamaster.httpclient.psi.HttpMethod

class HttpExecutionResult(httpMethod: HttpMethod, selectedEnv: String?) :
    ExecutionResult {
    private val httpProcessHandler = HttpProcessHandler(httpMethod, selectedEnv)

    override fun getExecutionConsole(): ExecutionConsole {
        return HttpExecutionConsole(httpProcessHandler.getComponent())
    }

    override fun getActions(): Array<AnAction> {
        return arrayOf()
    }

    override fun getProcessHandler(): ProcessHandler {
        return httpProcessHandler
    }
}
