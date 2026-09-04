package org.javamaster.httpclient.js.support.jsObject

import org.javamaster.httpclient.consts.HttpConsts.Companion.REQUEST_RAW
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.req.HttpClientRequest
import org.javamaster.httpclient.js.support.req.RequestVariables
import org.mozilla.javascript.ScriptableObject

@Suppress("unused")
object CommonVariables {
    lateinit var environment: MutableMap<String, String>
    var global = JsGlobalVariablesHolder
    lateinit var file: MutableMap<String, String>
    lateinit var request: RequestVariables

    fun get(key: String): Any? {
        val jsExecutor = JsExecutor.Companion.threadLocal.get()!!

        var value = jsExecutor.getRequestVariable(key)
        if (value != null) {
            return value
        }

        val request = ScriptableObject.getProperty(jsExecutor.reqScriptableObject, REQUEST_RAW) as HttpClientRequest
        value = request.fileVariables[key]
        if (value != null) {
            return value
        }

        value = JsGlobalVariablesHolder.get(key)
        if (value != null) {
            return value
        }

        value = request.environment[key]
        if (value != null) {
            return value
        }

        return null
    }

    override fun toString(): String {
        return javaClass.simpleName
    }
}