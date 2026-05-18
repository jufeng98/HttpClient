package org.javamaster.httpclient.js.support

import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.jsObject.Console
import org.javamaster.httpclient.js.support.jsObject.JavaBridge
import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
class JsHandlerPredefineRequestVariables {

    companion object {
        private val initRequestJsStr by lazy {
            val url = Companion::class.java.classLoader.getResource("js/initRequest.js")!!
            url.readText(StandardCharsets.UTF_8)
        }

        fun defineConsole(global: ScriptableObject) {
            val console = Context.javaToJS(Console, global)
            ScriptableObject.putProperty(global, "console", console)
        }

        fun defineJavaBridge(reqScriptableObject: ScriptableObject, jsExecutor: JsExecutor) {
            val javaBridge = Context.javaToJS(JavaBridge(jsExecutor), reqScriptableObject)
            ScriptableObject.putProperty(reqScriptableObject, "javaBridge", javaBridge)
        }

        fun defineRandom(reqScriptableObject: ScriptableObject) {
            JsExecutor.context.evaluateString(reqScriptableObject, initRequestJsStr, "initRequest.js", 1, null)
        }

    }

}