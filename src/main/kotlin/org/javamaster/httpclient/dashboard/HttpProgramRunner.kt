package org.javamaster.httpclient.dashboard

import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.ExecutionManagerImpl.Companion.getAllDescriptors
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.dashboard.HttpExecutor.Companion.HTTP_EXECUTOR_ID
import org.javamaster.httpclient.mock.MockServer
import org.javamaster.httpclient.mock.support.MockServerHelper
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.processHandler.ProcessHandlerBase
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.runconfig.HttpRunProfileState
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.ConfigUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.ws.WsRequest


/**
 * @author yudong
 */
class HttpProgramRunner : GenericProgramRunner<RunnerSettings>() {

    override fun getRunnerId(): String {
        return HTTP_RUNNER_ID
    }

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return if (profile !is HttpRunConfiguration) false
        else {
            DefaultRunExecutor.EXECUTOR_ID == executorId || DefaultDebugExecutor.EXECUTOR_ID == executorId
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

        val methodText = httpMethod.text
        if (methodText == HttpRequestEnum.MOCK_SERVER.name) {
            val request = PsiTreeUtil.getParentOfType(httpMethod, HttpRequest::class.java)!!
            val requestTarget = request.requestTarget
            if (requestTarget == null) {
                loadingRemover?.run()
                return
            }

            val port = MockServerHelper.resolvePort(requestTarget.port)
            if (MockServer.isRunning(port)) {
                NotifyUtil.notifyWarn(project, NlsBundle.nls("mock.server.running", port))
                loadingRemover?.run()
                showWindow(project, tabName)
                return
            }
        } else if (methodText == HttpRequestEnum.WEBSOCKET.name) {
            if (WsRequest.isRunning(tabName)) {
                NotifyUtil.notifyWarn(project, NlsBundle.nls("req.running", tabName))
                loadingRemover?.run()
                showWindow(project, tabName)
                return
            }
        } else {
            if (ProcessHandlerBase.isRunning(tabName)) {
                NotifyUtil.notifyWarn(project, NlsBundle.nls("req.running", tabName))
                loadingRemover?.run()
                showWindow(project, tabName)
                return
            }
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

        val handler = executionResult.processHandler as ProcessHandlerBase

        val oldDescriptor = getAllDescriptors(environment.project)
            .firstOrNull {
                it.processHandler is ProcessHandlerBase && it.displayName == handler.tabName
            }

        if (oldDescriptor != null) {
            oldDescriptor.isSelectContentWhenAdded = false

            val oldProcessHandler = oldDescriptor.processHandler as ProcessHandlerBase

            Disposer.dispose(oldProcessHandler)
        }

        environment.contentToReuse = oldDescriptor

        return RunContentBuilder(executionResult, environment).showRunContent(oldDescriptor)
    }

    private fun showWindow(project: Project, tabName: String) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow(ToolWindowId.SERVICES)
        toolWindow?.show()

        val descriptor = getAllDescriptors(project)
            .firstOrNull {
                it.processHandler is ProcessHandlerBase && it.displayName == tabName
            }

        if (descriptor != null) {
            RunContentManager.getInstance(project).selectRunContent(descriptor)
        }
    }

    companion object {
        const val HTTP_RUNNER_ID = "HttpProgramRunner"
    }
}