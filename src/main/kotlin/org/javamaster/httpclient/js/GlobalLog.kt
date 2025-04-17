package org.javamaster.httpclient.js

import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.utils.HttpUtils

/**
 * Collect js executed log
 */
object GlobalLog {
    private var tabName: String? = null
    private val logsMap = LinkedMultiValueMap<String, String>()

    fun setTabName(tmpKey: String) {
        tabName = tmpKey
    }

    fun log(msg: String?) {
        logsMap.add(tabName!!, msg ?: "null")
    }

    fun getAndClearLogs(): String {
        val logs = logsMap[tabName] ?: emptyList()

        clearLogs()

        return logs.joinToString(HttpUtils.CR_LF)
    }

    fun clearLogs() {
        logsMap.remove(tabName)
        tabName = null
    }
}
