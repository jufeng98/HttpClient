package org.javamaster.httpclient.js

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.readText
import org.javamaster.httpclient.annos.JsBridge
import org.javamaster.httpclient.utils.HttpUtils
import org.mozilla.javascript.ScriptableObject
import java.io.File


@Suppress("unused")
class JavaBridge(private val jsScriptExecutor: JsScriptExecutor) {

    @JsBridge(jsFun = "require(path)")
    fun require(path: String): ScriptableObject {
        val filePath = HttpUtils.constructFilePath(path, jsScriptExecutor.parentPath)
        val file = File(filePath)

        val virtualFile = VfsUtil.findFileByIoFile(file, true)
            ?: throw IllegalArgumentException("js文件不存在:${file.normalize()}")

        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
        val jsStr = document?.text ?: virtualFile.readText()

        jsScriptExecutor.context.evaluateString(jsScriptExecutor.global, jsStr, file.name, 1, null)

        return jsScriptExecutor.global
    }

    @JsBridge(jsFun = "readString(path)")
    fun readString(path: String): String {
        val filePath = HttpUtils.constructFilePath(path, jsScriptExecutor.parentPath)
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("文件不存在:${file.normalize()}")
        }

        val virtualFile = VfsUtil.findFileByIoFile(file, true)!!
        return virtualFile.readText()
    }

}
