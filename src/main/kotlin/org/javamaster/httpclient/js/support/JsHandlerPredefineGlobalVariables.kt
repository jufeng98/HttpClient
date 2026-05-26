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

        fun defineWindow(global: ScriptableObject, context: Context) {
            val window = Context.javaToJS(Window, global, context)
            ScriptableObject.putProperty(global, "Window", window)
        }

        fun defineCrypto(global: ScriptableObject, context: Context) {
            val crypto = Context.javaToJS(CryptoSupport, global, context)
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

        fun defineClient(global: ScriptableObject, context: Context) {
            val client = Context.javaToJS(HttpRequest, global, context)
            ScriptableObject.putProperty(global, "client", client)
        }

        fun defineSystemProperty(global: ScriptableObject, context: Context) {
            val properties = System.getProperties()
            val obj = JsonParser(context, global).parseValue(gsonNotPretty.toJson(properties))
            ScriptableObject.putProperty(global, PROPERTY_PREFIX, obj)
        }

        fun defineSystemEnv(global: ScriptableObject, context: Context) {
            val env = System.getenv()
            val obj = JsonParser(context, global).parseValue(gsonNotPretty.toJson(env))
            ScriptableObject.putProperty(global, ENV_PREFIX, obj)
        }

        fun defineJavaBridge(global: ScriptableObject, context: Context) {
            val javaBridge = Context.javaToJS(JavaBridge(), global, context)
            ScriptableObject.putProperty(global, "javaBridge", javaBridge)
        }

        fun defineRandom(global: ScriptableObject, context: Context) {
            context.evaluateString(global, initRequestJsStr, "initRequest.js", 1, null)
        }

    }
}