package org.javamaster.httpclient.utils

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId
import java.io.File

object PluginUtils {
    const val NAME = "HttpRequest"

    fun getPluginPath(): File {
        val pluginId = PluginId.findId("org.javamaster.HttpRequest")!!
        val pluginDescriptor = PluginManager.getInstance().findEnabledPlugin(pluginId)!!
        return pluginDescriptor.pluginPath.toFile()
    }
}
