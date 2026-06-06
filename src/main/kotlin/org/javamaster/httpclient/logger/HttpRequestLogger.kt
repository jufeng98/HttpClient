package org.javamaster.httpclient.logger

import com.intellij.openapi.diagnostic.Logger

private val log = Logger.getInstance("HttpRequest")

fun logWarn(msg: String, t: Throwable? = null) {
    System.err.println("HttpRequest: $msg")
    t?.printStackTrace()
    log.warn(msg, t)
}

fun logInfo(msg: String) {
    println("HttpRequest: $msg")
    log.info(msg)
}