package org.javamaster.httpclient.utils

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.runconfig.HttpConfigurationType
import org.javamaster.httpclient.runconfig.HttpRunConfiguration

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

    }

}
