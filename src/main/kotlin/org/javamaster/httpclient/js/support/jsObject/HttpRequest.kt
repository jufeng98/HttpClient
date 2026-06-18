package org.javamaster.httpclient.js.support.jsObject

import org.javamaster.httpclient.exception.HttpFileException
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.GlobalLog
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function

/**
 * @author yudong
 */
@Suppress("unused")
object HttpRequest {
    val global: JsGlobalVariablesHolder = JsGlobalVariablesHolder
    val variables: CommonVariables = CommonVariables

    fun log(first: Any?) {
        Console.log(first)
    }

    fun log(first: Any?, sec: Any?) {
        Console.log(first, sec)
    }

    fun log(first: Any?, sec: Any?, third: Any?, vararg arguments: Any?) {
        Console.log(first, sec, third, arguments)
    }

    fun test(testName: String, callback: Function) {
        val context = Context.enter()
        try {
            callback.call(
                context,
                JsExecutor.Companion.globalScriptableObject,
                JsExecutor.Companion.globalScriptableObject,
                arrayOf()
            )
            GlobalLog.log(testName)
        } finally {
            Context.exit()
        }
    }

    fun assert(condition: Boolean, message: String) {
        if (!condition) {
            throw HttpFileException(message)
        }
    }

    fun exit() {
        throw HttpFileException("Exit")
    }

    override fun toString(): String {
        return "HttpRequest(global=$global, variables=$variables)"
    }

}