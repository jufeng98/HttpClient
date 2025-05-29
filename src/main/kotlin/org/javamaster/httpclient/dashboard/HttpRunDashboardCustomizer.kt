package org.javamaster.httpclient.dashboard

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.dashboard.RunDashboardCustomizer
import com.intellij.execution.dashboard.RunDashboardRunConfigurationNode
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.ide.projectView.PresentationData
import com.intellij.psi.PsiElement
import com.intellij.ui.SimpleTextAttributes
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.getTargetHttpMethod

/**
 * Support Service tool window double click action, jump to request of the corresponding file.
 *
 * @author yudong
 */
class HttpRunDashboardCustomizer : RunDashboardCustomizer() {

    override fun isApplicable(settings: RunnerAndConfigurationSettings, descriptor: RunContentDescriptor?): Boolean {
        return settings.configuration is HttpRunConfiguration
    }

    override fun getPsiElement(node: RunDashboardRunConfigurationNode): PsiElement? {
        val configuration = node.configurationSettings.configuration as HttpRunConfiguration
        return getTargetHttpMethod(configuration.httpFilePath, configuration.name, configuration.project)
    }

    override fun updatePresentation(presentation: PresentationData, node: RunDashboardRunConfigurationNode): Boolean {
        val processHandler = node.descriptor?.processHandler as HttpProcessHandler? ?: return false

        if (processHandler.hasError) {
            presentation.addText(" Status: Error in the request", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            return true
        }

        val status = processHandler.httpStatus ?: return false

        val icon = HttpUtils.pickMethodIcon(processHandler.httpMethod.text)

        presentation.setIcon(icon)

        val costTimes = processHandler.costTimes
        presentation.addText(" Status: $status($costTimes ms)", SimpleTextAttributes.GRAYED_ATTRIBUTES)

        return true
    }
}
