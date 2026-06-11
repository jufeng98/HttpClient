package org.javamaster.httpclient.js.support.func

import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.factory.HttpWrapFactory
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.VirtualFileUtils
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import java.io.File

/**
 * @author yudong
 */
class RequireFunction(private val jsExecutor: JsExecutor) : HttpBaseFunction() {
    private val loaded = mutableSetOf<String>()

    override fun callInner(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val reqScriptableObject = jsExecutor.reqScriptableObject

        val path = args!![0] as String
        val parentPath = jsExecutor.parentPath

        val filePath = HttpUtils.constructFilePath(path, parentPath)
        val file = File(filePath)

        val absolutePath = file.absolutePath
        if (loaded.contains(absolutePath)) {
            return reqScriptableObject
        }

        loaded.add(absolutePath)

        val jsStr = VirtualFileUtils.readNewestContent(file)

        val context = Context.enter()
        try {
            context.evaluateString(reqScriptableObject, jsStr, file.name, 1, null)
        } finally {
            Context.exit()
        }

        return reqScriptableObject
    }

    override fun getArity(): Int {
        return 1
    }

}
