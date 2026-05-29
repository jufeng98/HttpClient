package org.javamaster.httpclient.js

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.concurrentMapOf
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts.Companion.REQUEST_RAW
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
import org.javamaster.httpclient.utils.XmlUtils
import org.mozilla.javascript.*
import org.mozilla.javascript.json.JsonParser
import java.io.FileNotFoundException

/**
 * Execute the previous and post request js scripts
 *
 * @author yudong
 */
class JsExecutor(val project: Project, val parentPath: String, val tabName: String) {
    internal val reqScriptableObject: ScriptableObject
    private var request: HttpClientRequest? = null

    init {
        val context = Context.enter()

        reqScriptableObject = context.initStandardObjects()
        reqScriptableObject.prototype = globalScriptableObject

        JsHandlerPredefineRequestVariables.defineConsole(reqScriptableObject, context)

        JsHandlerPredefineRequestFunctions.defineDateFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineSleepFunc(reqScriptableObject)

        JsHandlerPredefineRequestFunctions.defineTimestampDateFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineTimestampFullFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineBase64ToFileFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineFileToBase64Func(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineReadStringFunc(reqScriptableObject, this)

        JsHandlerPredefineRequestFunctions.defineRequireFunc(reqScriptableObject, this)

        Context.exit()
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
        ScriptableObject.putProperty(reqScriptableObject, REQUEST_RAW, request)
    }

    fun evalJsBeforeRequest(preJsFiles: List<PreJsFile>, jsListBeforeReq: List<HttpScriptBody>): List<String> {
        if (jsListBeforeReq.isEmpty() && preJsFiles.isEmpty()) {
            return mutableListOf()
        }

        val context = Context.enter()
        try {
            GlobalLog.setTabName(tabName)
            threadLocal.set(this)

            val resList = mutableListOf("/*$CR_LF${nls("pre.desc")}:$CR_LF")

            val preFilePair = preJsFiles.partition { it.urlFile != null }

            val npmFiles = preFilePair.first

            executeNpmFiles(npmFiles, context)

            val preFiles = preFilePair.second

            preFiles.forEach {
                val fileName = it.file.name

                evalJs(it.content, 1, fileName, reqScriptableObject, context)
            }

            if (jsListBeforeReq.isNotEmpty()) {
                val virtualFile = jsListBeforeReq[0].containingFile.virtualFile
                val document = FileDocumentManager.getInstance().getDocument(virtualFile)!!

                jsListBeforeReq.forEach {
                    val rowNum = document.getLineNumber(it.textOffset) + 1

                    evalJs(it.text, rowNum, virtualFile.name, reqScriptableObject, context)
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
            Context.exit()
        }
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

        val context = Context.enter()
        try {
            GlobalLog.setTabName(tabName)
            threadLocal.set(this)

            val reqBodyInJs = ReqUtils.convertReqBody(reqBody)

            val resBodyStr = httpResInfo.bodyStr
            val typeEnum = httpResInfo.simpleTypeEnum

            val body: Any
            when (typeEnum) {
                SimpleTypeEnum.JSON -> body = JsonParser(context, globalScriptableObject).parseValue(resBodyStr!!)

                SimpleTypeEnum.HTML -> body = resBodyStr!!

                SimpleTypeEnum.XML -> body = XmlUtils.parseXml(resBodyStr!!)

                SimpleTypeEnum.TEXT -> body = resBodyStr!!

                SimpleTypeEnum.TXT -> body = resBodyStr!!

                else -> body = httpResInfo.bodyBytes
            }

            val response = HttpClientResponse(statusCode, ResponseHeaders(headerMap), body, cookies)

            ScriptableObject.putProperty(reqScriptableObject, "request", HttpClientRequestRes(url, reqBodyInJs))

            ScriptableObject.putProperty(reqScriptableObject, "response", response)

            val virtualFile = jsScript.containingFile.virtualFile
            val document = FileDocumentManager.getInstance().getDocument(virtualFile)!!
            val rowNum = document.getLineNumber(jsScript.textOffset) + 1

            try {
                evalJs(jsScript.text, rowNum, virtualFile.name, reqScriptableObject, context)
            } catch (e: Exception) {
                GlobalLog.log("$e")
            }

            return GlobalLog.getAndClearLogs()
        } catch (e: Exception) {
            GlobalLog.clearLogs()

            throw e
        } finally {
            threadLocal.remove()
            Context.exit()
        }
    }

    /**
     * Execute library js file first, so the pre and post js handler can access library obj
     */
    private fun executeNpmFiles(npmFiles: List<PreJsFile>, context: Context) {
        if (npmFiles.isEmpty()) {
            return
        }

        val libraryScriptableObjects = npmFiles
            .map {
                val fileName = it.file.name
                var scriptableObject = libraryLoadedMap[fileName]

                if (scriptableObject == null) {
                    scriptableObject = context.initStandardObjects()

                    evalJs(it.content, 1, fileName, scriptableObject, context)

                    libraryLoadedMap[fileName] = scriptableObject

                    println("Loaded and cached js library: $fileName")
                }

                scriptableObject!!
            }

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

        libraryScriptableObjects.last().prototype = globalScriptableObject
    }

    private fun evalJs(
        jsStr: String,
        rowNum: Int,
        fileName: String,
        scriptableObject: ScriptableObject,
        context: Context,
    ) {
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
        internal val threadLocal = ThreadLocal<JsExecutor>()

        private val libraryLoadedMap = concurrentMapOf<String, ScriptableObject>()

        val globalScriptableObject: ScriptableObject by lazy {
            val context = Context.enter()
            val globalTmp = context.initStandardObjects()

            JsHandlerPredefineGlobalVariables.defineWindow(globalTmp, context)

            JsHandlerPredefineGlobalVariables.defineSystemProperty(globalTmp, context)

            JsHandlerPredefineGlobalVariables.defineSystemEnv(globalTmp, context)

            JsHandlerPredefineGlobalVariables.defineCrypto(globalTmp, context)

            JsHandlerPredefineGlobalVariables.defineURLSearchParams(globalTmp, context)

            JsHandlerPredefineGlobalVariables.defineClient(globalTmp, context)

            JsHandlerPredefineGlobalVariables.defineJavaBridge(globalTmp, context)

            JsHandlerPredefineGlobalVariables.defineRandom(globalTmp, context)

            JsHandlerPredefineGlobalFunctions.defineXpathFunc(globalTmp)

            JsHandlerPredefineGlobalFunctions.defineJsonPathFunc(globalTmp)

            Context.exit()

            globalTmp
        }
    }
}
