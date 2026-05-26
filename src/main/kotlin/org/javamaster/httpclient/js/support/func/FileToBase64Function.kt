package org.javamaster.httpclient.js.support.func

import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.js.JsExecutor
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

/**
 * @author yudong
 */
class FileToBase64Function(private val jsExecutor: JsExecutor) : HttpBaseFunction() {

    override fun callInner(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val path = args!![0] as String
        val parentPath = jsExecutor.httpFile.virtualFile.parent.path

        val enum = InnerVariableEnum.FILE_TO_BASE64
        return enum.exec(enum.methodName, parentPath, path)
    }

    override fun getArity(): Int {
        return 1
    }

}