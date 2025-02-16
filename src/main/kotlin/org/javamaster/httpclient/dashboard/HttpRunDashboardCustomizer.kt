package org.javamaster.httpclient.dashboard

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.dashboard.RunDashboardCustomizer
import com.intellij.execution.dashboard.RunDashboardRunConfigurationNode
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.utils.HttpUtils.getTargetHttpMethod

/**
 * 支持 Service 工具窗口的双击动作,跳转到对应的文件请求
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

}
