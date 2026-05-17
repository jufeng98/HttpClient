package org.javamaster.httpclient.js.support

import org.javamaster.httpclient.js.GlobalLog
import org.javamaster.httpclient.js.JsExecutor
import org.mozilla.javascript.NativeFunction

@Suppress("unused")
object HttpRequest {
    val global: GlobalVariables = GlobalVariables

    fun log(vararg arguments: Any?) {
        Console.log(*arguments)
    }

    fun test(testName: String, callback: NativeFunction) {
        callback.call(JsExecutor.context, JsExecutor.global, JsExecutor.global, arrayOf())
        GlobalLog.log(testName)
    }

    fun assert(condition: Boolean, message: String) {
        if (!condition) {
            throw Error(message)
        }
    }

    fun exit() {
        throw Error("Exit")
    }
}