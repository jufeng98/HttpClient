package org.javamaster.httpclient.runconfig

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpConfigurationFactory : SimpleConfigurationType(
    HttpUtils.HTTP_TYPE_ID,
    "HttpClient",
    "Use to send request",
    NotNullLazyValue.createConstantValue(HttpIcons.FILE)
) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return HttpRunConfiguration(project, this, "")
    }

}
