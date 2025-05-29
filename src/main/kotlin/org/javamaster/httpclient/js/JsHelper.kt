package org.javamaster.httpclient.js

/**
 * @author yudong
 */
object JsHelper {
    private var jsExecutorInitialed = false

    fun alreadyInit(){
        jsExecutorInitialed = true
    }

    fun getJsGlobalVariable(name: String): String? {
        if (!jsExecutorInitialed) {
            return null
        }

        return JsExecutor.JsGlobalVariableMap?.get(name)
    }
}
