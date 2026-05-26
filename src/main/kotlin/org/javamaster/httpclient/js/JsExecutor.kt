package org.javamaster.httpclient.js

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.exception.HttpFileException
import org.javamaster.httpclient.exception.JsFileException
import org.javamaster.httpclient.js.support.*
import org.javamaster.httpclient.js.support.jsObject.Cookie
import org.javamaster.httpclient.js.support.req.*
import org.javamaster.httpclient.js.support.res.HttpClientRequestRes
import org.javamaster.httpclient.js.support.res.HttpClientResponse
import org.javamaster.httpclient.js.support.res.ResponseHeaders
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpReqInfo
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpScriptBody
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.ReqUtils
import org.mozilla.javascript.*
import org.mozilla.javascript.json.JsonParser
import org.xml.sax.InputSource
import java.io.FileNotFoundException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Execute the previous and post request js scripts (always executed in the EDT thread)
 *
 * @author yudong
 */
class JsExecutor(val project: Project, val httpFile: PsiFile, val tabName: String) {
    val reqScriptableObject: ScriptableObject = context.initStandardObjects()
    private var request: HttpClientRequest? = null

    init {
        reqScriptableObject.prototype = global

        JsHandlerPredefineRequestVariables.defineConsole(reqScriptableObject)

        JsHandlerPredefineRequestFunctions.defineDateFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineTimestampDateFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineTimestampFullFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineBase64ToFileFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineFileToBase64Func(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineReadStringFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineRequireFunc(reqScriptableObject, this)
    }

    fun initJsRequestObj(
        url: String,
        rawUrl: String,
        rawBody: String?,
        reqInfo: HttpReqInfo,
        method: HttpRequestEnum,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
        selectedEnv: String?,
        fileScopeVariableMap: LinkedHashMap<String, String>,
    ) {
        val environment = reqInfo.environment
        environment["selectedEnv"] = selectedEnv ?: ""

        val jsBody = ReqUtils.convertReqBody(reqInfo.reqBody)

        request = HttpClientRequest(
            environment,
            RequestUrl(url, rawUrl),
            RequestBody(jsBody, rawBody),
            method.name,
            RequestVariables(),
            fileScopeVariableMap,
            RequestHeaders(reqHeaderMap),
        )

        ScriptableObject.putProperty(reqScriptableObject, "request", request)
        ScriptableObject.putProperty(reqScriptableObject, "requestRaw", request)
    }

    fun evalJsBeforeRequest(preJsFiles: List<PreJsFile>, jsListBeforeReq: List<HttpScriptBody>): List<String> {
        if (jsListBeforeReq.isEmpty() && preJsFiles.isEmpty()) {
            return mutableListOf()
        }

        try {
            GlobalLog.setTabName(tabName)
            threadLocal.set(this)

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
        } finally {
            threadLocal.remove()
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
        cookies: List<Cookie>,
    ): String? {
        if (jsScript == null) {
            return null
        }

        try {
            GlobalLog.setTabName(tabName)
            threadLocal.set(this)

            val jsBody = ReqUtils.convertReqBody(reqBody)

            val body: Any
            val typeEnum = httpResInfo.simpleTypeEnum
            when (typeEnum) {
                SimpleTypeEnum.JSON -> {
                    body = JsonParser(context, global).parseValue(httpResInfo.bodyStr!!)
                }

                SimpleTypeEnum.HTML -> {
                    body = httpResInfo.bodyStr!!
                }

                SimpleTypeEnum.XML -> {
                    val documentBuilder = documentBuilderFactory.newDocumentBuilder()
                    body = documentBuilder.parse(InputSource(StringReader(httpResInfo.bodyStr!!)))
                }

                SimpleTypeEnum.TEXT -> {
                    body = httpResInfo.bodyStr!!
                }

                SimpleTypeEnum.TXT -> {
                    body = httpResInfo.bodyStr!!
                }

                else -> {
                    body = httpResInfo.bodyBytes
                }
            }

            val response = HttpClientResponse(
                statusCode,
                ResponseHeaders(headerMap),
                body,
                cookies
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
        } finally {
            threadLocal.remove()
        }
    }

    private fun evalJs(jsStr: String, rowNum: Int, fileName: String, scriptableObject: ScriptableObject) {
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
                rethrowException(e.stackTrace, cause.cause.toString(), fileName)
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

    fun getRequestVariable(key: String): Any? {
        return request?.variables?.get(key)
    }

    companion object {
        var threadLocal = ThreadLocal<JsExecutor>()
        private val libraryLoadedMap = mutableMapOf<String, ScriptableObject>()
        private val pair by lazy {
            val contextTmp = Context.enter()
            val globalTmp = contextTmp.initStandardObjects()

            JsHandlerPredefineGlobalVariables.defineWindow(globalTmp)

            JsHandlerPredefineGlobalVariables.defineSystemProperty(globalTmp, contextTmp)

            JsHandlerPredefineGlobalVariables.defineSystemEnv(globalTmp, contextTmp)

            JsHandlerPredefineGlobalVariables.defineCrypto(globalTmp)

            JsHandlerPredefineGlobalVariables.defineURLSearchParams(globalTmp, contextTmp)

            JsHandlerPredefineGlobalVariables.defineClient(globalTmp)

            JsHandlerPredefineGlobalVariables.defineJavaBridge(globalTmp)

            JsHandlerPredefineGlobalVariables.defineRandom(globalTmp, contextTmp)

            JsHandlerPredefineGlobalFunctions.defineXpathFunc(globalTmp)

            JsHandlerPredefineGlobalFunctions.defineJsonPathFunc(globalTmp)

            Pair(contextTmp, globalTmp)
        }
        val context: Context = pair.first
        val global: ScriptableObject = pair.second

        @Suppress("HttpUrlsUsage")
        private val documentBuilderFactory by lazy {
            val factory = DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            factory
        }
    }
}
