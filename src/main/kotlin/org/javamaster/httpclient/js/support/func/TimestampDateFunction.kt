package org.javamaster.httpclient.js.support.func

import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.jsObject.JavaBridge
import org.mozilla.javascript.BaseFunction
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

/**
 * @author yudong
 */
class TimestampDateFunction(private val jsExecutor: JsExecutor) : BaseFunction() {

    override fun call(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val day = JavaBridge.convertArg(args!![0])!!
        val parentPath = jsExecutor.httpFile.virtualFile.parent.path

        val enum = InnerVariableEnum.TIMESTAMP_DATE
        return enum.exec(enum.methodName, parentPath, day)
    }

    override fun getArity(): Int {
        return 1
    }

}