package org.javamaster.httpclient.dashboard

import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.util.Disposer
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.dashboard.HttpExecutor.Companion.HTTP_EXECUTOR_ID
import org.javamaster.httpclient.processHandler.ProcessHandlerBase
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.runconfig.HttpRunProfileState
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.ConfigUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction


/**
 * @author yudong
 */
class HttpProgramRunner : GenericProgramRunner<RunnerSettings>() {

    override fun getRunnerId(): String {
        return HTTP_RUNNER_ID
    }

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        if (profile !is HttpRunConfiguration) {
            return false
        }

        return DefaultRunExecutor.EXECUTOR_ID == executorId || DefaultDebugExecutor.EXECUTOR_ID == executorId
    }

    fun executeFromGutter(httpMethod: HttpMethod, loadingRemover: Runnable? = null) {
        val project = computeReadAction { httpMethod.project }
        val tabName = HttpUtils.getTabName(httpMethod)

        if (ConfigUtils.checkRequestRunning(httpMethod, tabName, project)) {
            runInEdt { loadingRemover?.run() }
            return
        }

        httpMethod.putUserData(HttpConsts.gutterIconLoadingKey, loadingRemover)

        val httpExecutor = ExecutorRegistry.getInstance().getExecutorById(HTTP_EXECUTOR_ID)!!

        val selectedEnv = HttpEditorTopForm.getSelectedEnv(project)

        val runnerAndConfigurationSettings = ConfigUtils.saveConfiguration(tabName, project, selectedEnv, httpMethod)

        val environment = ExecutionEnvironment(httpExecutor, this, runnerAndConfigurationSettings, project)

        execute(environment)
    }

    override fun doExecute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor? {
        if (state !is HttpRunProfileState) {
            return null
        }

        val project = environment.project
        val runnerAndConfigurationSettings = environment.runnerAndConfigurationSettings
        if (runnerAndConfigurationSettings != null) {
            val config = runnerAndConfigurationSettings.configuration as HttpRunConfiguration

            HttpEditorTopForm.setCurrentEditorSelectedEnv(config.httpFilePath, project, config.env)
        }

        environment.executionId = 0

        val executionResult = state.execute(environment.executor, this) ?: return null

        val handler = executionResult.processHandler as ProcessHandlerBase

        val oldDescriptor = RunContentManager.getInstance(project).allDescriptors
            .firstOrNull {
                it.processHandler is ProcessHandlerBase && it.displayName == handler.tabName
            }

        if (oldDescriptor != null) {
            oldDescriptor.isSelectContentWhenAdded = true
            Disposer.dispose(oldDescriptor.processHandler as ProcessHandlerBase)
        }

        environment.contentToReuse = oldDescriptor

        val descriptor = RunContentBuilder(executionResult, environment).showRunContent(oldDescriptor)
        descriptor.isSelectContentWhenAdded = true

        return descriptor
    }

    companion object {
        const val HTTP_RUNNER_ID = "HttpProgramRunner"
    }
}