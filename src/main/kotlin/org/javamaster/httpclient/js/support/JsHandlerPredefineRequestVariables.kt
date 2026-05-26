package org.javamaster.httpclient.js.support

import org.javamaster.httpclient.js.support.jsObject.Console
import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject

/**
 * @author yudong
 */
class JsHandlerPredefineRequestVariables {

    companion object {

        fun defineConsole(global: ScriptableObject, context: Context) {
            val console = Context.javaToJS(Console, global, context)
            ScriptableObject.putProperty(global, "console", console)
        }

    }

}