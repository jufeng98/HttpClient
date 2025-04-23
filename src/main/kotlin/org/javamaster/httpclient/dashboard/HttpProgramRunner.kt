package org.javamaster.httpclient.dashboard

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.impl.ExecutionManagerImpl.Companion.getAllDescriptors
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.vfs.JarFileSystem
import org.javamaster.httpclient.dashboard.HttpExecutor.Companion.HTTP_EXECUTOR_ID
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.runconfig.HttpRunProfileState
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.NotifyUtil
import java.nio.file.InvalidPathException
import java.nio.file.Path


class HttpProgramRunner : GenericProgramRunner<RunnerSettings>() {

    override fun getRunnerId(): String {
        return HTTP_RUNNER_ID
    }

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return if (profile !is HttpRunConfiguration) false
        else {
            "Debug" == executorId || "Run" == executorId
        }
    }

    fun executeFromGutter(httpMethod: HttpMethod, gutterComponent: EditorGutterComponentEx?) {
        val loadingRemover = gutterComponent?.setLoadingIconForCurrentGutterMark()

        val project = httpMethod.project

        if (httpMethod.containingFile.virtualFile.fileSystem is JarFileSystem) {
            NotifyUtil.notifyWarn(project, NlsBundle.nls("template.not.execute"))
            loadingRemover?.run()
            return
        }

        val tabName = HttpUtils.getTabName(httpMethod)

        try {
            // tabName会用作文件名,因此需要检测下
            Path.of(tabName)

            if (tabName.contains("/") || tabName.contains("\\")) {
                throw InvalidPathException(tabName, NlsBundle.nls("tab.name.error", "Illegal char: / \\"))
            }
        } catch (e: InvalidPathException) {
            NotifyUtil.notifyError(project, NlsBundle.nls("tab.name.error", e.message!!))
            loadingRemover?.run()
            return
        }

        httpMethod.putUserData(HttpUtils.gutterIconLoadingKey, loadingRemover)

        val httpExecutor = ExecutorRegistry.getInstance().getExecutorById(HTTP_EXECUTOR_ID)!!

        val selectedEnv = HttpEditorTopForm.getSelectedEnv(httpMethod.project)

        val runnerAndConfigurationSettings = HttpUtils.saveConfiguration(tabName, project, selectedEnv, httpMethod)

        val environment = ExecutionEnvironment(httpExecutor, this, runnerAndConfigurationSettings, project)

        execute(environment)
    }

    @Throws(ExecutionException::class)
    override fun doExecute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor? {
        if (state !is HttpRunProfileState) {
            return null
        }

        val runnerAndConfigurationSettings = environment.runnerAndConfigurationSettings
        if (runnerAndConfigurationSettings != null) {
            val httpRunConfiguration = runnerAndConfigurationSettings.configuration as HttpRunConfiguration
            HttpEditorTopForm.setCurrentEditorSelectedEnv(
                httpRunConfiguration.httpFilePath,
                environment.project,
                httpRunConfiguration.env
            )
        }

        environment.executionId = 0

        val executionResult = state.execute(environment.executor, this) ?: return null

        val console = executionResult.executionConsole as HttpExecutionConsole
        val handler = executionResult.processHandler as HttpProcessHandler

        environment.contentToReuse = getAllDescriptors(environment.project)
            .firstOrNull {
                it.processHandler is HttpProcessHandler && it.displayName == handler.tabName
            }

        return HttpRunContentDescriptor(console, handler, console.component, handler.tabName)
    }

    companion object {
        const val HTTP_RUNNER_ID = "HttpProgramRunner"
    }
}