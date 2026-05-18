package org.javamaster.httpclient.js.support

import org.javamaster.httpclient.js.support.func.JsonPathFunction
import org.javamaster.httpclient.js.support.func.XpathFunction
import org.mozilla.javascript.ScriptableObject

/**
 * @author yudong
 */
class JsHandlerPredefineGlobalFunctions {

    companion object {

        fun defineXpathFunc(global: ScriptableObject) {
            ScriptableObject.putProperty(global, "xpath", XpathFunction)
        }

        fun defineJsonPathFunc(global: ScriptableObject) {
            ScriptableObject.putProperty(global, "jsonPath", JsonPathFunction)
        }

    }

}