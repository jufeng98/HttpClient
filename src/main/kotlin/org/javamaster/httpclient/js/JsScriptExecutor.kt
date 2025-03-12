package org.javamaster.httpclient.js

import com.intellij.openapi.components.Service
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
    private val reserveKeys: Set<String>

    private val documentBuilderFactory by lazy {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        factory
    }
    private val xPathFactory by lazy {
        XPathFactory.newInstance()
    }
    var xmlDoc: Document? = null
    var xPath: XPath? = null

    init {
        val set = mutableSetOf(
            "CryptoJS",
            "javaBridge",
            "client",
            "getLog",
            "hasGlobalVariableKey",
            "getGlobalVariable",
            "hasRequestVariableKey",
            "getRequestVariable",
            "console"
        )

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
            set.add(it.name)
        }

        reserveKeys = set

        url = javaClass.classLoader.getResource("examples/initGlobal.js")!!
        jsStr = url.readText(StandardCharsets.UTF_8)
        context.evaluateString(global, jsStr, "dummy.js", 1, null)

        url = javaClass.classLoader.getResource("examples/initRequest.js")!!
        initRequestJsStr = url.readText(StandardCharsets.UTF_8)
    }

    fun prepareJsRequestObj() {
        val jsonJs = """
            $PROPERTY_PREFIX = ${gson.toJson(System.getProperties())};
            $ENV_PREFIX = ${gson.toJson(System.getenv())};
        """.trimIndent()
        context.evaluateString(global, jsonJs, "dummy.js", 1, null)

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
        // 清除多余对象,防止污染全局 js 环境
        val nativeObject = global as NativeObject
        nativeObject.keys
            .forEach {
                val key = "" + it
                if (reserveKeys.contains(key)) {
                    return@forEach
                }

                global.remove(key)
                if (nativeObject.has(key, global)) {
                    ScriptableObject.putProperty(global, key, null)
                }
            }
    }

    fun evalJsBeforeRequest(beforeJsScripts: List<String>): List<String> {
        if (beforeJsScripts.isEmpty()) {
            return arrayListOf()
        }

        val resList = mutableListOf("/*\r\n前置js执行结果:\r\n")

        val list = beforeJsScripts
            .map { evalJsInAnonymousFun(it) }
            .filter { it.isNotEmpty() }

        resList.addAll(list)

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


        context.evaluateString(global, js, "dummy.js", 1, null)

        val res = evalJsInAnonymousFun(jsScript)

        context.evaluateString(global, "delete response;", "dummy.js", 1, null)

        xmlDoc = null
        xPath=null

        return res
    }

    private fun evalJsInAnonymousFun(jsScript: String): String {
        try {
            val js = "(function(){'use strict';${jsScript}}())"
            context.evaluateString(global, js, "dummy.js", 0, null)
        } catch (e: Exception) {
            return e.message + "\r\n"
        }

        return ScriptableObject.callMethod(global, "getLog", arrayOf()) as String
    }

    fun getRequestVariable(key: String): String? {
        if (!ScriptableObject.hasProperty(global, "request")) {
            return null
        }

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

    fun getJsGlobalVariables(): Map<String, String> {
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
