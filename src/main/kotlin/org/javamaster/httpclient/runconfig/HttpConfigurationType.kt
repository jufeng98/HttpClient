package org.javamaster.httpclient.runconfig

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.openapi.util.NotNullLazyValue
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.consts.HttpConsts.Companion.HTTP_TYPE_ID

/**
 * @author yudong
 */
class HttpConfigurationType : ConfigurationTypeBase(
    HTTP_TYPE_ID,
    "HttpClient",
    "Use to send request",
    NotNullLazyValue.createConstantValue(HttpIcons.FILE)
) {
    init {
        addFactory(HttpConfigurationFactory())
    }
}