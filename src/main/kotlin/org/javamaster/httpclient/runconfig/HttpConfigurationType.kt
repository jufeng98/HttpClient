package org.javamaster.httpclient.runconfig

import com.intellij.execution.configurations.ConfigurationTypeBase
import org.javamaster.httpclient.consts.HttpConsts.Companion.HTTP_CLIENT
import org.javamaster.httpclient.consts.HttpConsts.Companion.HTTP_CLIENT_ICON
import org.javamaster.httpclient.consts.HttpConsts.Companion.HTTP_TYPE_ID

/**
 * @author yudong
 */
class HttpConfigurationType : ConfigurationTypeBase(
    HTTP_TYPE_ID,
    HTTP_CLIENT,
    "Use to send request",
    HTTP_CLIENT_ICON
) {
    init {
        addFactory(HttpConfigurationFactory())
    }
}