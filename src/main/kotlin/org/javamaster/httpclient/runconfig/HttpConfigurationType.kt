package org.javamaster.httpclient.runconfig

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.NotNullLazyValue
import org.javamaster.httpclient.HttpIcons

/**
 * @author yudong
 */
class HttpConfigurationType : ConfigurationTypeBase(
    "gitFlowPlusHttpClient",
    "HttpClient",
    "Use to send request",
    NotNullLazyValue.createConstantValue(HttpIcons.FILE)
) {
    init {
        addFactory(HttpConfigurationFactory())
    }
}
