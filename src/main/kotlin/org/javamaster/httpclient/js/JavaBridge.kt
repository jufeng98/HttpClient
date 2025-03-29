package org.javamaster.httpclient.js

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.readText
import com.jayway.jsonpath.JsonPath
import org.javamaster.httpclient.annos.JsBridge
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJavaObject
import org.mozilla.javascript.ScriptableObject
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*


@Suppress("unused")
class JavaBridge(private val jsExecutor: JsExecutor) {
    private val parentPath = jsExecutor.httpFile.virtualFile.parent.path

    @JsBridge(jsFun = "require(path)")
    fun require(path: String): ScriptableObject {
        val scriptableObject = jsExecutor.reqScriptableObject

        val filePath = HttpUtils.constructFilePath(path, parentPath)
        val file = File(filePath)

        val virtualFile = VfsUtil.findFileByIoFile(file, true)
            ?: throw FileNotFoundException("js文件不存在:${file.normalize()}")

        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
        val jsStr = document?.text ?: virtualFile.readText()

        JsExecutor.context.evaluateString(scriptableObject, jsStr, file.name, 1, null)

        return scriptableObject
    }

    @JsBridge(jsFun = "readString(path)")
    fun readString(path: String): String {
        val filePath = HttpUtils.constructFilePath(path, parentPath)
        val file = File(filePath)

        val virtualFile = VfsUtil.findFileByIoFile(file, true)
            ?: throw FileNotFoundException("文件不存在:${file.normalize()}")

        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
        return document?.text ?: virtualFile.readText()
    }

    @JsBridge(jsFun = "getBodyArray()")
    fun getBodyArray(): Any {
        return Context.javaToJS(jsExecutor.bodyArray!!, jsExecutor.reqScriptableObject)
    }

    @JsBridge(jsFun = "getXmlDoc()")
    fun getXmlDoc(): Any {
        val resObj = Context.javaToJS(jsExecutor.xmlDoc!!, jsExecutor.reqScriptableObject) as NativeJavaObject
        resObj.prototype = JsExecutor.context.initStandardObjects()
        return resObj
    }

    @JsBridge(jsFun = "evaluateXPath(expression)")
    fun evaluateXPath(expression: String): Any? {
        return jsExecutor.xPath!!.evaluate(expression, jsExecutor.xmlDoc!!)
    }

    @JsBridge(jsFun = "evaluateJsonPath(expression)")
    fun evaluateJsonPath(expression: String): Any? {
        return JsonPath.read(jsExecutor.jsonStr, expression)
    }


    @JsBridge(jsFun = "xpath(obj, expression)")
    fun xpath(obj: Any, expression: String): Any? {
        return JsExecutor.xPathFactory.newXPath().evaluate(expression, obj)
    }

    @JsBridge(jsFun = "jsonPath(obj, expression)")
    fun jsonPath(obj: Any, expression: String): Any? {
        return JsonPath.read(obj, expression)
    }

    @JsBridge(jsFun = "btoa(bytes)")
    fun btoa(bytes: String): String {
        return Base64.getEncoder().encodeToString(bytes.toByteArray())
    }

    @JsBridge(jsFun = "atob(str)")
    fun atob(str: String): String {
        return String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8)
    }

    @JsBridge(jsFun = "base64ToFile(base64, path)")
    fun base64ToFile(base64: String, path: String): Boolean {
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
            GlobalLog.log("完成转换base64并保存到文件:${file.normalize()}")

            VirtualFileManager.getInstance().asyncRefresh(null)

            return true
        } catch (e: Exception) {
            GlobalLog.log("base64ToFile处理失败:$e")
            return false
        }
    }
}
