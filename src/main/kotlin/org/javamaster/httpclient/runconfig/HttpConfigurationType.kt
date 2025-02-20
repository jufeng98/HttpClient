package org.javamaster.httpclient.runconfig

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.NotNullLazyValue
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpConfigurationType : ConfigurationTypeBase(
    HttpUtils.HTTP_TYPE_ID,
    "HttpClient",
    "Use to send request",
    NotNullLazyValue.createConstantValue(HttpIcons.FILE)
) {
    init {
        addFactory(HttpConfigurationFactory())
    }
}