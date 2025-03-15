package org.javamaster.httpclient.js

import com.intellij.openapi.project.Project
import org.javamaster.httpclient.annos.JsBridge
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.resolve.VariableResolver.Companion.ENV_PREFIX
import org.javamaster.httpclient.resolve.VariableResolver.Companion.PROPERTY_PREFIX
import org.javamaster.httpclient.utils.HttpUtils.gson
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory

/**
 * 执行请求的前置js和后置js脚本(永远是在 EDT 线程中执行)
 *
 * @author yudong
 */
class JsExecutor(val project: Project, val parentPath: String, val tabName: String) {
    val reqScriptableObject: ScriptableObject by lazy {
        val scriptableObject = context.initStandardObjects()
        scriptableObject.prototype = global

        // 注册js桥接对象 javaBridge
        val javaBridge = Context.javaToJS(JavaBridge(this), scriptableObject)
        ScriptableObject.putProperty(scriptableObject, "javaBridge", javaBridge)

        context.evaluateString(scriptableObject, javaBridgeJsStr, "javaBridge.js", 1, null)

        context.evaluateString(scriptableObject, initRequestJsStr, "initRequest.js", 1, null)

        val jsonJs = """
            $PROPERTY_PREFIX = ${gson.toJson(System.getProperties())};
            $ENV_PREFIX = ${gson.toJson(System.getenv())};
        """.trimIndent()
        context.evaluateString(scriptableObject, jsonJs, "initPropertiesAndEnv.js", 1, null)

        scriptableObject
    }

    var xmlDoc: Document? = null
    var xPath: XPath? = null

    fun initJsRequestBody(reqBody: Any) {
        val js = """
            request.body = {
                string: () => {
                    return `$reqBody`
                }
            }
        """.trimIndent()
        context.evaluateString(reqScriptableObject, js, "initRequestBody.js", 1, null)
    }

    fun evalJsBeforeRequest(beforeJsScripts: List<String>): List<String> {
        if (beforeJsScripts.isEmpty()) {
            return mutableListOf()
        }

        GlobalLog.setTabName(tabName)

        val resList = mutableListOf("/*\r\n前置js执行结果:\r\n")

        beforeJsScripts.forEach { evalJsInAnonymousFun(it) }

        resList.add(GlobalLog.getAndClearLogs() + "\r\n")

        resList.add("*/\r\n")

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

        GlobalLog.setTabName(tabName)

        val headerJsonStr = gson.toJson(headers)

        val js: String
        val typeEnum = resPair.first
        when (typeEnum) {
            SimpleTypeEnum.JSON -> {
                val bytes = resPair.second
                val jsonStr = String(bytes, StandardCharsets.UTF_8)
                js = """
                  var response = {
                    status: ${statusCode},
                    body: $jsonStr,
                    headers: $headerJsonStr
                  };
                """.trimIndent()
            }

            SimpleTypeEnum.XML -> {
                val bytes = resPair.second
                val xmlStr = String(bytes, StandardCharsets.UTF_8)

                val documentBuilder = documentBuilderFactory.newDocumentBuilder()

                xmlDoc = documentBuilder.parse(InputSource(StringReader(xmlStr)))
                xPath = xPathFactory.newXPath()

                js = """   
                  var response = {
                    status: ${statusCode},
                    body: javaBridge.getXmlDoc(),
                    headers: $headerJsonStr
                  };
                  response.body.xPath = {
                    evaluate: function(xPath) {
                        return javaBridge.evaluate(xPath);
                    },
                  }
                """.trimIndent()
            }

            else -> {
                js = """
                  var response = {
                    status: ${statusCode},
                    body: {},
                    headers: $headerJsonStr
                  };
                """.trimIndent()
            }
        }

        context.evaluateString(reqScriptableObject, js, "initResponseBody.js", 1, null)

        try {
            evalJsInAnonymousFun(jsScript)
        } catch (e: Exception) {
            GlobalLog.log(e.message)
        }

        context.evaluateString(reqScriptableObject, "delete response;", "dummy.js", 1, null)

        return GlobalLog.getAndClearLogs()
    }

    private fun evalJsInAnonymousFun(jsScript: String) {
        try {
            val js = "(function () { 'use strict'; ${jsScript.trim()} })();"
            context.evaluateString(reqScriptableObject, js, "anonymous.js", 1, null)
        } catch (e: Exception) {
            GlobalLog.log(e.message)
        }
    }

    fun getRequestVariable(key: String): String? {
        if (!ScriptableObject.hasProperty(reqScriptableObject, "request")) {
            return null
        }

        val hasKey = ScriptableObject.callMethod(reqScriptableObject, "hasRequestVariableKey", arrayOf(key)) as Boolean
        if (!hasKey) {
            return null
        }

        val res = ScriptableObject.callMethod(reqScriptableObject, "getRequestVariable", arrayOf(key)) ?: return "null"
        return res.toString()
    }

    fun getGlobalVariable(key: String): String? {
        val hasKey = ScriptableObject.callMethod(reqScriptableObject, "hasGlobalVariableKey", arrayOf(key)) as Boolean
        if (!hasKey) {
            return null
        }

        val res = ScriptableObject.callMethod(reqScriptableObject, "getGlobalVariable", arrayOf(key)) ?: return "null"
        return res.toString()
    }

    fun getJsGlobalVariables(): Map<String, String> {
        val dataHolder = context.evaluateString(
            reqScriptableObject, "client.global.dataHolder", "dummy.js",
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
        private val pair by lazy {
            val context: Context = Context.enter()
            val global: ScriptableObject = context.initStandardObjects()

            var url = Companion::class.java.classLoader.getResource("examples/crypto-js.js")!!
            var jsStr = url.readText(StandardCharsets.UTF_8)
            context.evaluateString(global, jsStr, "crypto-js.js", 1, null)
            // 注册 CryptoJS 对象
            global.prototype.put("CryptoJS", global, global.get("CryptoJS"))

            val globalLog = Context.javaToJS(GlobalLog, global)
            ScriptableObject.putProperty(global, "globalLog", globalLog)

            url = Companion::class.java.classLoader.getResource("examples/initGlobal.js")!!
            jsStr = url.readText(StandardCharsets.UTF_8)
            context.evaluateString(global, jsStr, "initGlobal.js", 1, null)

            Pair(context, global)
        }
        val context: Context = pair.first
        val global: ScriptableObject = pair.second

        private val javaBridgeJsStr by lazy {
            JavaBridge::class.java.declaredMethods
                .joinToString("\r\n") {
                    val jsBridge = it.getAnnotation(JsBridge::class.java) ?: return@joinToString ""
                    """
                        function ${jsBridge.jsFun} {
                            return javaBridge.${jsBridge.jsFun};                    
                        }
                    """.trimIndent()
                }
        }

        private val initRequestJsStr by lazy {
            val url = Companion::class.java.classLoader.getResource("examples/initRequest.js")!!
            url.readText(StandardCharsets.UTF_8)
        }

        private val documentBuilderFactory by lazy {
            val factory = DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            factory
        }

        private val xPathFactory by lazy {
            XPathFactory.newInstance()
        }
    }
}
