package org.javamaster.httpclient.js

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.annos.JsBridge
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.utils.HttpUtils.gson
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject
import java.nio.charset.StandardCharsets

/**
 * 执行请求的前置js和后置js脚本
 *
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class JsScriptExecutor {
    lateinit var parentPath: String
    lateinit var project: Project
    val context: Context = Context.enter()
    val global: ScriptableObject = context.initStandardObjects()
    private val initRequestJsStr: String

    init {
        var url = javaClass.classLoader.getResource("examples/crypto-js.js")!!
        var jsStr = url.readText(StandardCharsets.UTF_8)
        context.evaluateString(global, jsStr, "dummy.js", 1, null)
        // 注册 CryptoJS 对象
        global.prototype.put("CryptoJS", global, global.get("CryptoJS"))

        val javaBridge = Context.javaToJS(JavaBridge(this), global)
        // 注册js桥接对象 javaBridge
        ScriptableObject.putProperty(global, "javaBridge", javaBridge)
        // 注册全局js函数
        JavaBridge::class.java.declaredMethods.forEach {
            val jsBridge = it.getAnnotation(JsBridge::class.java)
            val js = """
                function ${jsBridge.jsFun} {
                    return javaBridge.${jsBridge.jsFun};                    
                }
            """.trimIndent()
            context.evaluateString(global, js, "dummy.js", 1, null)
        }

        url = javaClass.classLoader.getResource("examples/initGlobal.js")!!
        jsStr = url.readText(StandardCharsets.UTF_8)
        context.evaluateString(global, jsStr, "dummy.js", 1, null)

        url = javaClass.classLoader.getResource("examples/initRequest.js")!!
        initRequestJsStr = url.readText(StandardCharsets.UTF_8)
    }

    fun prepareJsRequestObj() {
        context.evaluateString(global, initRequestJsStr, "dummy.js", 1, null)
    }

    fun initJsRequestBody(reqBody: Any) {
        val js = """
            request.body = {
                string: () => {
                    return `$reqBody`
                }
            }
        """.trimIndent()
        context.evaluateString(global, js, "dummy.js", 1, null)
    }

    fun clearJsRequestObj() {
        context.evaluateString(global, "delete request;", "dummy.js", 1, null)
    }

    fun evalJsBeforeRequest(beforeJsScripts: List<String>): List<String> {
        if (beforeJsScripts.isEmpty()) {
            return arrayListOf()
        }

        val resList = mutableListOf("// 前置js执行结果:\r\n")

        val list = beforeJsScripts
            .map { evalJsInAnonymousFun(it) }
            .filter { it.isNotEmpty() }

        resList.addAll(list)
        return resList
    }

    fun evalJsAfterRequest(
        jsScript: String?,
        resPair: Pair<SimpleTypeEnum, ByteArray>,
        statusCode: Int,
        headers: MutableMap<String, MutableList<String>>,
    ): String? {
        if (jsScript == null) {
            return null
        }

        val headerJsonStr = gson.toJson(headers)

        val body: String
        if (resPair.first == SimpleTypeEnum.JSON) {
            val bytes = resPair.second
            val jsonStr = String(bytes, StandardCharsets.UTF_8)
            body = jsonStr
        } else {
            body = "{}"
        }

        val js = """
              var response = {
                status: ${statusCode},
                body: $body,
                headers: $headerJsonStr
              };
            """.trimIndent()
        context.evaluateString(global, js, "dummy.js", 1, null)

        val res = evalJsInAnonymousFun(jsScript)

        context.evaluateString(global, "delete response;", "dummy.js", 1, null)

        return res
    }

    private fun evalJsInAnonymousFun(jsScript: String): String {
        try {
            val js = "(function(){'use strict';${jsScript}}())"
            context.evaluateString(global, js, "dummy.js", 0, null)
        } catch (e: Exception) {
            return "# " + e.message + "\r\n"
        }

        return ScriptableObject.callMethod(global, "getLog", arrayOf()) as String
    }

    fun getRequestVariable(key: String): String? {
        val hasKey = ScriptableObject.callMethod(global, "hasRequestVariableKey", arrayOf(key)) as Boolean
        if (!hasKey) {
            return null
        }

        val res = ScriptableObject.callMethod(global, "getRequestVariable", arrayOf(key)) ?: return "null"
        return res.toString()
    }

    fun getGlobalVariable(key: String): String? {
        val hasKey = ScriptableObject.callMethod(global, "hasGlobalVariableKey", arrayOf(key)) as Boolean
        if (!hasKey) {
            return null
        }

        val res = ScriptableObject.callMethod(global, "getGlobalVariable", arrayOf(key)) ?: return "null"
        return res.toString()
    }

    fun getGlobalVariables(): Map<String, String> {
        val dataHolder = context.evaluateString(
            global, "client.global.dataHolder", "dummy.js",
            1, null
        ) as NativeObject

        val map = mutableMapOf<String, String>()
        dataHolder.entries.forEach {
            map[it.key.toString()] = if (it.value != null) {
                it.value.toString()
            } else {
                "null"
            }
        }

        return map
    }

    companion object {
        fun getService(project: Project): JsScriptExecutor {
            return project.getService(JsScriptExecutor::class.java)
        }
    }
}
