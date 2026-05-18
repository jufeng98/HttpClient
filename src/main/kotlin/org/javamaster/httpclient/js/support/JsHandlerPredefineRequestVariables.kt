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

        fun defineConsole(global: ScriptableObject) {
            val console = Context.javaToJS(Console, global)
            ScriptableObject.putProperty(global, "console", console)
        }

    }

}