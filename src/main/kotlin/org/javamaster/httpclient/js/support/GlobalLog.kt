package org.javamaster.httpclient.js.support

import com.jetbrains.rd.util.threadLocalWithInitial
import org.javamaster.httpclient.logger.HttpRequestLogger.logInfo
import org.javamaster.httpclient.utils.HttpUtils

/**
 * Collect js executed log
 */
object GlobalLog {
    private val logThreadLocal = threadLocalWithInitial { mutableListOf<String>() }

    fun log(msg: String?) {
        val str = msg ?: "null"
        logInfo(str)
        logThreadLocal.get().add(str)
    }

    fun getAndClearLogs(): String {
        val logs = logThreadLocal.get()

        logThreadLocal.remove()

        return logs.joinToString(HttpUtils.CR_LF)
    }

}