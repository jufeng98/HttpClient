package org.javamaster.httpclient.js.support.func

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
class ReadStringFunction(private val jsExecutor: JsExecutor) : BaseFunction() {

    override fun call(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val path = args!![0] as String

        val parentPath = jsExecutor.httpFile.virtualFile.parent.path

        val filePath = HttpUtils.constructFilePath(path, parentPath)
        val file = File(filePath)

        return VirtualFileUtils.readNewestContent(file)
    }

    override fun getArity(): Int {
        return 1
    }

}