package org.javamaster.httpclient.dashboard

import com.intellij.execution.ExecutionResult
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.openapi.actionSystem.AnAction
import org.javamaster.httpclient.processHandler.ProcessHandlerBase
import org.javamaster.httpclient.psi.HttpMethod

class HttpExecutionResult(httpMethod: HttpMethod, selectedEnv: String?) :
    ExecutionResult {
    private val myProcessHandler = ProcessHandlerBase.createProcessHandler(httpMethod, selectedEnv)

    override fun getExecutionConsole(): ExecutionConsole {
        return HttpExecutionConsole(myProcessHandler.getComponent())
    }

    override fun getActions(): Array<AnAction> {
        return arrayOf()
    }

    override fun getProcessHandler(): ProcessHandler {
        return myProcessHandler
    }
}
