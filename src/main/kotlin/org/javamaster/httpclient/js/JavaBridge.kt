package org.javamaster.httpclient.js

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.readText
import com.jayway.jsonpath.JsonPath
import org.javamaster.httpclient.annos.JsBridge
import org.javamaster.httpclient.utils.HttpUtils
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJavaObject
import org.mozilla.javascript.ScriptableObject
import java.io.File
import java.io.FileNotFoundException


@Suppress("unused")
class JavaBridge(private val jsExecutor: JsExecutor) {

    @JsBridge(jsFun = "require(path)")
    fun require(path: String): ScriptableObject {
        val scriptableObject = jsExecutor.reqScriptableObject

        val filePath = HttpUtils.constructFilePath(path, jsExecutor.parentPath)
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
        val filePath = HttpUtils.constructFilePath(path, jsExecutor.parentPath)
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

}
