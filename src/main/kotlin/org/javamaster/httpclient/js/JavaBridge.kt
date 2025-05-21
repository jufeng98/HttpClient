package org.javamaster.httpclient.js

import com.intellij.openapi.vfs.VirtualFileManager
import com.jayway.jsonpath.JsonPath
import org.javamaster.httpclient.annos.JsBridge
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.exception.HttpFileException
import org.javamaster.httpclient.exception.JsFileException
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.VirtualFileUtils
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJavaObject
import org.mozilla.javascript.ScriptableObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*


@Suppress("unused")
class JavaBridge(private val jsExecutor: JsExecutor) {
    private val parentPath = jsExecutor.httpFile.virtualFile.parent.path
    val headerMap: LinkedMultiValueMap<String, String> = LinkedMultiValueMap()

    @JsBridge(jsFun = "require(path)")
    fun require(path: String): ScriptableObject {
        val scriptableObject = jsExecutor.reqScriptableObject

        val filePath = HttpUtils.constructFilePath(path, parentPath)
        val file = File(filePath)

        val jsStr = VirtualFileUtils.readNewestContent(file)

        try {
            JsExecutor.context.evaluateString(scriptableObject, jsStr, file.name, 1, null)
        } catch (e: Exception) {
            throw JsFileException(e.toString(), e)
        }

        return scriptableObject
    }

    @JsBridge(jsFun = "readString(path)")
    fun readString(path: String): String {
        val filePath = HttpUtils.constructFilePath(path, parentPath)
        val file = File(filePath)

        return VirtualFileUtils.readNewestContent(file)
    }

    @JsBridge(jsFun = "getBodyArray()")
    fun getBodyArray(): Any {
        return Context.javaToJS(jsExecutor.bodyArray!!, jsExecutor.reqScriptableObject)
    }

    @JsBridge(jsFun = "getBodyString()")
    fun getBodyString(): String {
        return String(jsExecutor.bodyArray!!, StandardCharsets.UTF_8)
    }

    @JsBridge(jsFun = "convertBodyToByteArray(str)")
    fun convertBodyToByteArray(str: String): Any? {
        if (str == "null") {
            return null
        }

        return Context.javaToJS(str.toByteArray(StandardCharsets.UTF_8), jsExecutor.reqScriptableObject)
    }

    @JsBridge(jsFun = "getXmlDoc()")
    fun getXmlDoc(): Any {
        val resObj = Context.javaToJS(jsExecutor.xmlDoc!!, jsExecutor.reqScriptableObject) as NativeJavaObject
        resObj.prototype = JsExecutor.context.initStandardObjects()
        return resObj
    }

    @JsBridge(jsFun = "evaluateXPath(expression)")
    fun evaluateXPath(expression: String): Any? {
        try {
            return jsExecutor.xPath!!.evaluate(expression, jsExecutor.xmlDoc!!)
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

    @JsBridge(jsFun = "evaluateJsonPath(expression)")
    fun evaluateJsonPath(expression: String): Any? {
        try {
            return JsonPath.read(jsExecutor.jsonStr, expression)
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }


    @JsBridge(jsFun = "xpath(obj, expression)")
    fun xpath(obj: Any, expression: String): Any? {
        try {
            return JsExecutor.xPathFactory.newXPath().evaluate(expression, obj)
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

    @JsBridge(jsFun = "jsonPath(obj, expression)")
    fun jsonPath(obj: Any, expression: String): Any? {
        try {
            return JsonPath.read(obj, expression)
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

    @JsBridge(jsFun = "btoa(bytes)")
    fun btoa(bytes: String): String {
        try {
            return Base64.getEncoder().encodeToString(bytes.toByteArray())
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

    @JsBridge(jsFun = "atob(str)")
    fun atob(str: String): String {
        try {
            return String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

    @JsBridge(jsFun = "base64ToFile(base64, path)")
    fun base64ToFile(base64: String, path: String) {
        try {
            val tmpPath = VariableResolver.resolveInnerVariable(path, parentPath, jsExecutor.project)

            val filePath = HttpUtils.constructFilePath(tmpPath, parentPath)

            val file = File(filePath)
            val parentFile = file.parentFile

            if (!parentFile.exists()) {
                parentFile.mkdirs()
            } else {
                if (file.exists()) {
                    file.delete()
                }
            }

            val bytes = Base64.getDecoder().decode(base64)
            val toPath = file.toPath()

            Files.write(toPath, bytes, StandardOpenOption.CREATE)

            GlobalLog.log(NlsBundle.nls("base64.convert.to.file") + " ${file.normalize()}")

            VirtualFileManager.getInstance().asyncRefresh(null)
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

    @JsBridge(jsFun = "setHeader(name, value)")
    fun setHeader(name: String, value: String?) {
        val list = LinkedList<String>()
        val str = value ?: ""
        list.add(str)

        headerMap[name] = list

        GlobalLog.log(NlsBundle.nls("req.header.set", name, str))
    }

    @JsBridge(jsFun = "addHeader(name, value)")
    fun addHeader(name: String, value: String?) {
        val str = value ?: ""

        headerMap.add(name, str)

        GlobalLog.log(NlsBundle.nls("req.header.add", name, str))
    }

    @JsBridge(jsFun = "callJava(methodName, arg0, arg1)")
    fun callJava(methodName: String, arg0: Any, arg1: Any): String {
        try {
            val args = mutableListOf<Any>()

            var convertArg = convertArg(arg0)
            if (convertArg != null) {
                args.add(convertArg)
            }

            convertArg = convertArg(arg1)
            if (convertArg != null) {
                args.add(convertArg)
            }

            val variableEnum = InnerVariableEnum.getEnum(methodName)
                ?: throw IllegalArgumentException(NlsBundle.nls("method.not.exists", methodName))

            return variableEnum.exec("", *args.toTypedArray())
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

    private fun convertArg(arg: Any): Any? {
        if (arg == "undefined") {
            return null
        }
        if (arg is Double) {
            return arg.toInt()
        }

        return arg
    }
}
