package org.javamaster.httpclient.utils

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.mock.MockServer
import org.javamaster.httpclient.mock.support.MockServerHelper
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.processHandler.ProcessHandlerBase
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.runconfig.HttpConfigurationType
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.ws.WsRequest

/**
 * @author yudong
 */
class ConfigUtils {

    companion object {

        fun saveConfiguration(
            tabName: String,
            project: Project,
            selectedEnv: String?,
            httpMethod: HttpMethod,
        ): RunnerAndConfigurationSettings {
            val runManager = RunManager.getInstance(project)

            var configurationSettings = runManager.allSettings
                .firstOrNull {
                    it.configuration is HttpRunConfiguration && it.configuration.name == tabName
                }

            val configNotExists = configurationSettings == null

            val httpRunConfiguration: HttpRunConfiguration
            if (configNotExists) {
                configurationSettings = runManager.createConfiguration(tabName, HttpConfigurationType::class.java)
                httpRunConfiguration = configurationSettings.configuration as HttpRunConfiguration
            } else {
                httpRunConfiguration = configurationSettings!!.configuration as HttpRunConfiguration
            }

            configurationSettings.isActivateToolWindowBeforeRun = false

            httpRunConfiguration.env = selectedEnv ?: ""
            httpRunConfiguration.httpFilePath = httpMethod.containingFile.virtualFile.path

            if (configNotExists) {
                runManager.addConfiguration(configurationSettings)
            }

            runManager.selectedConfiguration = configurationSettings

            return configurationSettings
        }

        fun checkRequestRunning(httpMethod: HttpMethod, tabName: String, project: Project): Boolean {
            val methodText = httpMethod.text
            if (methodText == HttpRequestEnum.MOCK_SERVER.name) {
                val request = PsiTreeUtil.getParentOfType(httpMethod, HttpRequest::class.java)!!
                val requestTarget = request.requestTarget
                if (requestTarget == null) {
                    return true
                }

                val port = MockServerHelper.resolvePort(requestTarget.port)
                if (MockServer.isRunning(port)) {
                    NotifyUtil.notifyWarn(project, NlsBundle.nls("mock.server.running", port))
                    showServicesWindow(project, tabName)
                    return true
                }
            } else if (methodText == HttpRequestEnum.WEBSOCKET.name) {
                if (WsRequest.isRunning(tabName)) {
                    NotifyUtil.notifyWarn(project, NlsBundle.nls("req.running", tabName))
                    showServicesWindow(project, tabName)
                    return true
                }
            } else {
                if (ProcessHandlerBase.isRunning(tabName)) {
                    NotifyUtil.notifyWarn(project, NlsBundle.nls("req.running", tabName))
                    showServicesWindow(project, tabName)
                    return true
                }
            }

            return false
        }

        private fun showServicesWindow(project: Project, tabName: String) {
            ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.SERVICES)?.show()

            val runContentManager = RunContentManager.getInstance(project)

            val descriptor = runContentManager.allDescriptors
                .firstOrNull {
                    it.processHandler is ProcessHandlerBase && it.displayName == tabName
                }

            if (descriptor != null) {
                runContentManager.selectRunContent(descriptor)
            }
        }

    }

}
