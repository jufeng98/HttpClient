package org.javamaster.httpclient.js.support

import com.jetbrains.rd.util.threadLocalWithInitial
import org.javamaster.httpclient.logger.HttpRequestLogger.logInfo

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

    fun getAndClearLogs(): List<String> {
        val logs = logThreadLocal.get() ?: emptyList<String>()

        logThreadLocal.remove()

        return logs
    }

}