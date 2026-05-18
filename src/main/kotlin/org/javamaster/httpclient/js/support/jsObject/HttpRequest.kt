package org.javamaster.httpclient.js.support.jsObject

import org.javamaster.httpclient.exception.HttpFileException
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.GlobalLog
import org.javamaster.httpclient.js.support.GlobalVariables
import org.mozilla.javascript.Function

/**
 * @author yudong
 */
@Suppress("unused")
object HttpRequest {
    val global: GlobalVariables = GlobalVariables

    fun log(vararg arguments: Any?) {
        Console.log(*arguments)
    }

    fun test(testName: String, callback: Function) {
        callback.call(JsExecutor.Companion.context, JsExecutor.Companion.global, JsExecutor.Companion.global, arrayOf())
        GlobalLog.log(testName)
    }

    fun assert(condition: Boolean, message: String) {
        if (!condition) {
            throw HttpFileException(message, null)
        }
    }

    fun exit() {
        throw HttpFileException("Exit", null)
    }
}