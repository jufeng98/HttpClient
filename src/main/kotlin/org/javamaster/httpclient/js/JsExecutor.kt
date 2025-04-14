package org.javamaster.httpclient.js

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.annos.JsBridge
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.exception.HttpFileException
import org.javamaster.httpclient.exception.JsFileException
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpReqInfo
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.psi.HttpScriptBody
import org.javamaster.httpclient.resolve.VariableResolver.Companion.ENV_PREFIX
import org.javamaster.httpclient.resolve.VariableResolver.Companion.PROPERTY_PREFIX
import org.javamaster.httpclient.utils.HttpUtils.gson
import org.mozilla.javascript.*
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.FileNotFoundException
import java.io.StringReader
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory

/**
 * Execute the previous and post request js scripts (always executed in the EDT thread)
 *
 * @author yudong
 */
class JsExecutor(val project: Project, val httpFile: PsiFile, val tabName: String) {
    val reqScriptableObject: ScriptableObject

    init {
        val scriptableObject = context.initStandardObjects()
        scriptableObject.prototype = global

        // Register js bridge object javaBridge
        val javaBridge = Context.javaToJS(JavaBridge(this), scriptableObject)
        ScriptableObject.putProperty(scriptableObject, "javaBridge", javaBridge)

        context.evaluateString(scriptableObject, javaBridgeJsStr, "javaBridge.js", 1, null)

        context.evaluateString(scriptableObject, initRequestJsStr, "initRequest.js", 1, null)

        val jsonJs = """
            $PROPERTY_PREFIX = ${gson.toJson(System.getProperties())};
            $ENV_PREFIX = ${gson.toJson(System.getenv())};
        """.trimIndent()
        context.evaluateString(scriptableObject, jsonJs, "initPropertiesAndEnv.js", 1, null)

        reqScriptableObject = scriptableObject
    }

    var bodyArray: ByteArray? = null
    var jsonStr: String? = null
    var xmlDoc: Document? = null
    var xPath: XPath? = null

    fun initJsRequestObj(reqInfo: HttpReqInfo, method: String, reqHeaderMap: LinkedMultiValueMap<String, String>) {
        val reqBody = reqInfo.reqBody
        val environment = reqInfo.environment

        val headers = gson.toJson(reqHeaderMap)

        var js = if (reqBody == null) {
            """
            request.body = {
                string: () => {
                    return null;
                },
                tryGetSubstituted: () => {
                    return null;
                }
            };
        """.trimIndent()
        } else {
            """
            request.body = {
                string: () => {
                    return `$reqBody`;
                },
                tryGetSubstituted: () => {
                    return `$reqBody`;
                }
            };
        """.trimIndent()
        }

        js += """
            request.method = '$method';
            request.environment = $environment;
            request.environment.get = function(name) {
                return this[name] !== undefined ? this[name] : null;
            };
            
            request.headers = $headers;
            request.headers.all = function() {
                return headersAll(this);
            };
            request.headers.findByName = function(name) {
                return headersFindByName(this, name);
            };
        """.trimIndent()

        context.evaluateString(reqScriptableObject, js, "initRequestBody.js", 1, null)
    }

    fun evalJsBeforeRequest(preJsFiles: List<PreJsFile>, jsListBeforeReq: List<HttpScriptBody>): List<String> {
        if (jsListBeforeReq.isEmpty() && preJsFiles.isEmpty()) {
            return mutableListOf()
        }

        try {
            GlobalLog.setTabName(tabName)

            val resList = mutableListOf("/*\r\nPre js executed result:\r\n")

            preJsFiles.forEach {
                evalJs(it.content, 1, it.file.name, it.url != null)
            }

            if (jsListBeforeReq.isNotEmpty()) {
                val virtualFile = jsListBeforeReq[0].containingFile.virtualFile
                val document = FileDocumentManager.getInstance().getDocument(virtualFile)!!

                jsListBeforeReq.forEach {
                    val rowNum = document.getLineNumber(it.textOffset) + 1

                    evalJs(it.text, rowNum, virtualFile.name, false)
                }
            }

            resList.add(GlobalLog.getAndClearLogs() + "\r\n")

            resList.add("*/\r\n")

            return resList
        } catch (e: Exception) {
            GlobalLog.getAndClearLogs()

            throw e
        }
    }

    fun evalJsAfterRequest(
        jsScript: HttpScriptBody?,
        resPair: Pair<SimpleTypeEnum, ByteArray>,
        statusCode: Int,
        headerMap: MutableMap<String, MutableList<String>>,
    ): String? {
        if (jsScript == null) {
            return null
        }

        GlobalLog.setTabName(tabName)

        val headers = gson.toJson(headerMap)

        var js: String
        val typeEnum = resPair.first
        when (typeEnum) {
            SimpleTypeEnum.JSON -> {
                val bytes = resPair.second
                jsonStr = String(bytes, StandardCharsets.UTF_8)

                js = """
                  // noinspection JSUnresolvedReference
                  // noinspection JSUnresolvedReference
                  var response = {
                    body: $jsonStr
                  };
                  response.body.jsonPath = {
                    evaluate: function(expression) {
                        return javaBridge.evaluateJsonPath(expression);
                    }
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
                  // noinspection JSUnresolvedReference
                  // noinspection JSUnresolvedReference
                  var response = {
                    body: javaBridge.getXmlDoc()
                  };
                  response.body.xpath = {
                    evaluate: function(expression) {
                        return javaBridge.evaluateXPath(expression);
                    }
                  };
                """.trimIndent()
            }

            SimpleTypeEnum.TEXT -> {
                val bytes = resPair.second
                val bodyText = String(bytes, StandardCharsets.UTF_8)

                js = """
                  // noinspection JSUnusedLocalSymbols
                  var response = {
                    body: `$bodyText`
                  };
                """.trimIndent()
            }

            else -> {
                bodyArray = resPair.second

                js = """
                  // noinspection JSUnusedLocalSymbols
                  // noinspection JSUnresolvedReference
                  var response = {
                    body: javaBridge.getBodyArray()
                  };
                """.trimIndent()
            }
        }

        js += """
            response.status = ${statusCode};
            response.headers = $headers;
            response.headers.all = function() {
                return headersAll(this);
            };
            response.headers.findByName = function(name) {
                return headersFindByName(this, name);
            };
            response.headers.valueOf = function(name) {
                return headersFindByName(this, name);
            };
            response.headers.valuesOf = function(name) {
                return headersFindListByName(this, name) || [];
            };
            response.contentType = resolveContentType(response.headers);
        """.trimIndent()

        context.evaluateString(reqScriptableObject, js, "initResponseBody.js", 1, null)

        val virtualFile = jsScript.containingFile.virtualFile
        val document = FileDocumentManager.getInstance().getDocument(virtualFile)!!
        val rowNum = document.getLineNumber(jsScript.textOffset) + 1

        try {
            evalJs(jsScript.text, rowNum, virtualFile.name, false)
        } catch (e: Exception) {
            GlobalLog.log("$e")
        }

        context.evaluateString(reqScriptableObject, "delete response;", "dummy.js", 1, null)

        return GlobalLog.getAndClearLogs()
    }

    private fun evalJs(jsStr: String, rowNum: Int, fileName: String, isLibrary: Boolean) {
        val scriptableObject = if (isLibrary) {
            libraryLoadedMap.computeIfAbsent(fileName) { context.initStandardObjects() }
        } else {
            reqScriptableObject
        }

        try {
            context.evaluateString(scriptableObject, jsStr, fileName, rowNum, null)
        } catch (e: WrappedException) {
            System.err.println("WrappedException")
            e.printStackTrace()

            val cause = e.cause
            if (cause is FileNotFoundException || cause is IllegalArgumentException || cause is HttpFileException) {
                rethrowException(e.stackTrace, cause.toString(), fileName)
            }

            if (cause is JsFileException) {
                throw cause.cause!!
            }

            throw EvaluatorException(e.wrappedException.toString(), fileName, e.lineNumber())
        } catch (e: JavaScriptException) {
            System.err.println("JavaScriptException")
            e.printStackTrace()

            rethrowException(e.stackTrace, e.toString(), fileName)
        } catch (e: Exception) {
            System.err.println("Exception")
            e.printStackTrace()

            throw e
        }
    }

    private fun rethrowException(staceTraces: Array<StackTraceElement>, message: String, fileName: String) {
        for (stackTraceElement in staceTraces) {
            if (stackTraceElement.fileName != fileName) {
                continue
            }

            throw EvaluatorException(message, fileName, stackTraceElement.lineNumber)
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
        private val libraryLoadedMap = mutableMapOf<String, ScriptableObject>()
        private val pair by lazy {
            val context: Context = Context.enter()
            val global: ScriptableObject = context.initStandardObjects()

            var url = Companion::class.java.classLoader.getResource("examples/crypto-js.js")!!
            var jsStr = url.readText(StandardCharsets.UTF_8)
            context.evaluateString(global, jsStr, "crypto-js.js", 1, null)
            // Register CryptoJS object
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

        @Suppress("HttpUrlsUsage")
        private val documentBuilderFactory by lazy {
            val factory = DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            factory
        }

        val xPathFactory: XPathFactory by lazy {
            XPathFactory.newInstance()
        }
    }
}
