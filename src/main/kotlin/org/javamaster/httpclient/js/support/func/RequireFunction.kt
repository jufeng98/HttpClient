package org.javamaster.httpclient.js.support.func

import org.javamaster.httpclient.exception.JsFileException
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.VirtualFileUtils
import org.mozilla.javascript.BaseFunction
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import java.io.File

/**
 * @author yudong
 */
class RequireFunction(private val jsExecutor: JsExecutor) : BaseFunction() {

    override fun call(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val path = args!![0] as String
        val parentPath = jsExecutor.httpFile.virtualFile.parent.path

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

    override fun getArity(): Int {
        return 1
    }

}
