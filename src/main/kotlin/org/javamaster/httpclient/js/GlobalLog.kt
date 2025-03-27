package org.javamaster.httpclient.js

import org.springframework.util.LinkedMultiValueMap

/**
 * 收集 js 执行日志
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

        logsMap.remove(tabName)
        tabName = null

        return logs.joinToString("\r\n")
    }
}
