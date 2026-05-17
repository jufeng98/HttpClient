package org.javamaster.httpclient.js

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.annos.JsBridge
import org.javamaster.httpclient.crypto.CryptoSupport
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.exception.HttpFileException
import org.javamaster.httpclient.exception.JsFileException
import org.javamaster.httpclient.js.support.*
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpReqInfo
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpScriptBody
import org.javamaster.httpclient.resolve.VariableResolver.Companion.ENV_PREFIX
import org.javamaster.httpclient.resolve.VariableResolver.Companion.PROPERTY_PREFIX
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.gson
import org.mozilla.javascript.*
import org.xml.sax.InputSource
import java.io.FileNotFoundException
import java.io.StringReader
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory

/**
 * Execute the previous and post request js scripts (always executed in the EDT thread)
 *
 * @author yudong
 */
class JsExecutor(val project: Project, val httpFile: PsiFile, val tabName: String) {
    val reqScriptableObject: ScriptableObject
    private val originalJavaBridge: JavaBridge

    init {
        val scriptableObject = context.initStandardObjects()
        scriptableObject.prototype = global

        // Register js bridge object javaBridge
        originalJavaBridge = JavaBridge(this)
        val javaBridge = Context.javaToJS(originalJavaBridge, scriptableObject)
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

    fun initJsRequestObj(
        url: String,
        rawUrl: String,
        rawBody: String?,
        reqInfo: HttpReqInfo,
        method: HttpRequestEnum,
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        selectedEnv: String?,
        fileScopeVariableMap: LinkedHashMap<String, String>,
    ) {
        val environment = reqInfo.environment
        environment["selectedEnv"] = selectedEnv ?: ""

        val jsBody = HttpUtils.convertReqBody(reqInfo.reqBody)

        val request = HttpClientRequest(
            environment,
            RequestUrl(url, rawUrl),
            RequestBody(jsBody, rawBody),
            method.name,
            fileScopeVariableMap,
            RequestHeaders(reqHeaderMap),
        )

        ScriptableObject.putProperty(reqScriptableObject, "request", request)
    }

    fun evalJsBeforeRequest(preJsFiles: List<PreJsFile>, jsListBeforeReq: List<HttpScriptBody>): List<String> {
        if (jsListBeforeReq.isEmpty() && preJsFiles.isEmpty()) {
            return mutableListOf()
        }

        try {
            GlobalLog.setTabName(tabName)

            val resList = mutableListOf("/*$CR_LF${nls("pre.desc")}:$CR_LF")

            val preFilePair = preJsFiles.partition { it.urlFile != null }

            val npmFiles = preFilePair.first

            executeNpmFiles(npmFiles)

            val preFiles = preFilePair.second

            preFiles.forEach {
                val fileName = it.file.name

                evalJs(it.content, 1, fileName, reqScriptableObject)
            }

            if (jsListBeforeReq.isNotEmpty()) {
                val virtualFile = jsListBeforeReq[0].containingFile.virtualFile
                val document = FileDocumentManager.getInstance().getDocument(virtualFile)!!

                jsListBeforeReq.forEach {
                    val rowNum = document.getLineNumber(it.textOffset) + 1

                    evalJs(it.text, rowNum, virtualFile.name, reqScriptableObject)
                }
            }

            resList.add(GlobalLog.getAndClearLogs() + CR_LF)

            resList.add("*/$CR_LF")

            return resList
        } catch (e: Exception) {
            GlobalLog.clearLogs()

            throw e
        }
    }

    /**
     * Execute library js file first, so the pre and post js handler can access library obj
     */
    private fun executeNpmFiles(npmFiles: List<PreJsFile>) {
        if (npmFiles.isEmpty()) {
            return
        }

        val libraryScriptableObjects = npmFiles
            .map {
                val fileName = it.file.name
                var scriptableObject = libraryLoadedMap[fileName]

                if (scriptableObject == null) {
                    scriptableObject = context.initStandardObjects()

                    evalJs(it.content, 1, fileName, scriptableObject)

                    libraryLoadedMap[fileName] = scriptableObject

                    println("Loaded and cached js library: $fileName")
                }

                scriptableObject!!
            }

        val global = reqScriptableObject.prototype

        var previous: ScriptableObject? = null

        libraryScriptableObjects.forEach {
            if (previous == null) {
                previous = it
                return@forEach
            }

            @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
            previous!!.prototype = it

            previous = it
        }


        reqScriptableObject.prototype = libraryScriptableObjects[0]

        libraryScriptableObjects.last().prototype = global
    }

    fun evalJsAfterRequest(
        url: String,
        reqBody: Any?,
        jsScript: HttpScriptBody?,
        httpResInfo: HttpResInfo,
        statusCode: Int,
        headerMap: MutableMap<String, MutableList<String>>,
    ): String? {
        if (jsScript == null) {
            return null
        }

        try {
            GlobalLog.setTabName(tabName)

            val jsBody = HttpUtils.convertReqBody(reqBody)

            val body: Any
            val typeEnum = httpResInfo.simpleTypeEnum
            when (typeEnum) {
                SimpleTypeEnum.JSON -> {
                    body = NativeObject()

                    val bodyMap = gson.fromJson(httpResInfo.bodyStr!!, Map::class.java)
                    body.putAll(bodyMap)
                }

                SimpleTypeEnum.XML -> {
                    val xmlStr = httpResInfo.bodyStr!!

                    val documentBuilder = documentBuilderFactory.newDocumentBuilder()

                    body = documentBuilder.parse(InputSource(StringReader(xmlStr)))
                }

                SimpleTypeEnum.TEXT -> {
                    body = httpResInfo.bodyStr!!
                }

                else -> {
                    body = httpResInfo.bodyBytes
                }
            }

            val response = HttpClientResponse(
                statusCode,
                ResponseHeaders(headerMap),
                body
            )

            ScriptableObject.putProperty(reqScriptableObject, "request", HttpClientRequestRes(url, jsBody))

            ScriptableObject.putProperty(reqScriptableObject, "response", response)

            val virtualFile = jsScript.containingFile.virtualFile
            val document = FileDocumentManager.getInstance().getDocument(virtualFile)!!
            val rowNum = document.getLineNumber(jsScript.textOffset) + 1

            try {
                evalJs(jsScript.text, rowNum, virtualFile.name, reqScriptableObject)
            } catch (e: Exception) {
                GlobalLog.log("$e")
            }

            return GlobalLog.getAndClearLogs()
        } catch (e: Exception) {
            GlobalLog.clearLogs()

            throw e
        }
    }

    private fun evalJs(jsStr: String, rowNum: Int, fileName: String, scriptableObject: ScriptableObject) {
        try {
            context.evaluateString(scriptableObject, jsStr, fileName, rowNum, null)

            JsGlobalVariableMap = getJsGlobalVariables()
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

    fun getJsGlobalVariable(key: String): String? {
        return GlobalVariables.get(key)?.toString()
    }

    fun getJsGlobalVariables(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        GlobalVariables.dataHolder
            .forEach {
                map[it.key.toString()] = if (it.value != null) {
                    it.value.toString()
                } else {
                    "null"
                }
            }

        return map
    }

    fun getHeaderMap(): LinkedMultiValueMap<String, String> {
        return originalJavaBridge.headerMap
    }

    companion object {
        private val libraryLoadedMap = mutableMapOf<String, ScriptableObject>()
        private val pair by lazy {
            val context: Context = Context.enter()
            val global: ScriptableObject = context.initStandardObjects()

            global.defineProperty("CONTENT_TRUNCATED", nls("content.truncated"), ScriptableObject.READONLY)

            // 改为使用 Java 实现 Crypto 相关
            // var url = Companion::class.java.classLoader.getResource("js/crypto-js.js")!!
            // var jsStr = url.readText(StandardCharsets.UTF_8)
            // context.evaluateString(global, jsStr, "crypto-js.js", 1, null)
            // Register CryptoJS object
            // global.prototype.put("CryptoJS", global, global.get("CryptoJS"))

            val globalLog = Context.javaToJS(GlobalLog, global)
            ScriptableObject.putProperty(global, "globalLog", globalLog)

            val console = Context.javaToJS(Console, global)
            ScriptableObject.putProperty(global, "console", console)

            val crypto = Context.javaToJS(CryptoSupport, global)
            ScriptableObject.putProperty(global, "crypto", crypto)

            val paramsClass = URLSearchParams::class.java
            ScriptableObject.putProperty(
                global,
                paramsClass.simpleName,
                context.getWrapFactory().wrapJavaClass(context, global, paramsClass)
            )

            val client = Context.javaToJS(HttpRequest, global)
            ScriptableObject.putProperty(global, "client", client)

            val url = Companion::class.java.classLoader.getResource("js/initGlobal.js")!!
            val jsStr = url.readText(StandardCharsets.UTF_8)
            context.evaluateString(global, jsStr, "initGlobal.js", 1, null)

            JsHelper.alreadyInit()

            Pair(context, global)
        }
        val context: Context = pair.first
        val global: ScriptableObject = pair.second

        private val javaBridgeJsStr by lazy {
            JavaBridge::class.java.declaredMethods
                .joinToString(CR_LF) {
                    val jsBridge = it.getAnnotation(JsBridge::class.java) ?: return@joinToString ""
                    """
                        function ${jsBridge.jsFun} {
                            return javaBridge.${jsBridge.jsFun};                    
                        }
                    """.trimIndent()
                }
        }

        private val initRequestJsStr by lazy {
            val url = Companion::class.java.classLoader.getResource("js/initRequest.js")!!
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

        var JsGlobalVariableMap: Map<String, String>? = null

        fun setGlobalVariable(key: String, value: String) {
            GlobalVariables.set(key, value)
        }
    }
}
