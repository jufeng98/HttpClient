package org.javamaster.httpclient.js.support.func

import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.VirtualFileUtils
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import java.io.File

/**
 * @author yudong
 */
class ReadStringFunction(private val jsExecutor: JsExecutor) : HttpBaseFunction() {

    override fun callInner(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val path = args!![0] as String

        val parentPath = jsExecutor.parentPath

        val filePath = HttpUtils.constructFilePath(path, parentPath)
        val file = File(filePath)

        return VirtualFileUtils.readNewestContent(file)
    }

    override fun getArity(): Int {
        return 1
    }

}