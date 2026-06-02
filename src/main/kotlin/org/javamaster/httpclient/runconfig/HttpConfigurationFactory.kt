package org.javamaster.httpclient.runconfig

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.consts.HttpConsts.Companion.HTTP_CLIENT
import org.javamaster.httpclient.consts.HttpConsts.Companion.HTTP_CLIENT_ICON
import org.javamaster.httpclient.consts.HttpConsts.Companion.HTTP_TYPE_ID

/**
 * @author yudong
 */
class HttpConfigurationFactory : SimpleConfigurationType(
    HTTP_TYPE_ID,
    HTTP_CLIENT,
    "Use to send request",
    HTTP_CLIENT_ICON
) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return HttpRunConfiguration(project, this, "")
    }

}
