package org.javamaster.httpclient.runconfig

import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.dashboard.HttpExecutionResult
import org.javamaster.httpclient.utils.HttpUtils.getTargetHttpMethod

/**
 * @author yudong
 */
class HttpRunProfileState(
    val project: Project,
    private val environment: ExecutionEnvironment,
    private val httpFilePath: String,
    private val selectedEnv: String?,
) : RunProfileState {

    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult? {
        val httpMethod = getTargetHttpMethod(httpFilePath, environment.runProfile.name, project) ?: return null

        return HttpExecutionResult(httpMethod, selectedEnv)
    }
}
