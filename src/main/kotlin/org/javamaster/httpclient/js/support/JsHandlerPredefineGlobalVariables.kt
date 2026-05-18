package org.javamaster.httpclient.js.support

import org.javamaster.httpclient.crypto.CryptoSupport
import org.javamaster.httpclient.js.support.jsObject.HttpRequest
import org.javamaster.httpclient.js.support.jsObject.JavaBridge
import org.javamaster.httpclient.js.support.jsObject.URLSearchParams
import org.javamaster.httpclient.js.support.jsObject.Window
import org.javamaster.httpclient.resolve.VariableResolver.Companion.ENV_PREFIX
import org.javamaster.httpclient.resolve.VariableResolver.Companion.PROPERTY_PREFIX
import org.javamaster.httpclient.utils.JsonUtils.gsonNotPretty
import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.json.JsonParser
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
class JsHandlerPredefineGlobalVariables {
    companion object {
        private val initRequestJsStr by lazy {
            val url = Companion::class.java.classLoader.getResource("js/initRequest.js")!!
            url.readText(StandardCharsets.UTF_8)
        }

        fun defineWindow(global: ScriptableObject) {
            val window = Context.javaToJS(Window, global)
            ScriptableObject.putProperty(global, "Window", window)
        }

        fun defineCrypto(global: ScriptableObject) {
            val crypto = Context.javaToJS(CryptoSupport, global)
            ScriptableObject.putProperty(global, "crypto", crypto)
        }

        fun defineURLSearchParams(global: ScriptableObject, context: Context) {
            val paramsClass = URLSearchParams::class.java
            ScriptableObject.putProperty(
                global,
                paramsClass.simpleName,
                context.getWrapFactory().wrapJavaClass(context, global, paramsClass)
            )
        }

        fun defineClient(global: ScriptableObject) {
            val client = Context.javaToJS(HttpRequest, global)
            ScriptableObject.putProperty(global, "client", client)
        }

        fun defineSystemProperty(global: ScriptableObject, contextTmp: Context) {
            val properties = System.getProperties()
            val obj = JsonParser(contextTmp, global).parseValue(gsonNotPretty.toJson(properties))
            ScriptableObject.putProperty(global, PROPERTY_PREFIX, obj)
        }

        fun defineSystemEnv(global: ScriptableObject, contextTmp: Context) {
            val env = System.getenv()
            val obj = JsonParser(contextTmp, global).parseValue(gsonNotPretty.toJson(env))
            ScriptableObject.putProperty(global, ENV_PREFIX, obj)
        }

        fun defineJavaBridge(global: ScriptableObject) {
            val javaBridge = Context.javaToJS(JavaBridge(), global)
            ScriptableObject.putProperty(global, "javaBridge", javaBridge)
        }

        fun defineRandom(global: ScriptableObject, context: Context) {
            context.evaluateString(global, initRequestJsStr, "initRequest.js", 1, null)
        }

    }
}