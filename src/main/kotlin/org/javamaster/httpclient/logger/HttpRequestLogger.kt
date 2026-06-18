package org.javamaster.httpclient.logger

import com.intellij.openapi.diagnostic.Logger

/**
 * @author yudong
 */
object HttpRequestLogger {
    private val log = Logger.getInstance("HttpRequest")

    fun logWarn(msg: String, t: Throwable? = null) {
        val currentThread = Thread.currentThread()

        System.err.println("[${currentThread.id} ${currentThread.name}] HttpRequest: $msg")
        t?.printStackTrace()

        log.info(msg, t)
    }

    fun logInfo(msg: String?) {
        val currentThread = Thread.currentThread()

        println("[${currentThread.id} ${currentThread.name}] HttpRequest: $msg")

        log.info(msg)
    }

}