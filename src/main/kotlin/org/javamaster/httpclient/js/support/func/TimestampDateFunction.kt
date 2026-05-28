package org.javamaster.httpclient.js.support.func

import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.jsObject.JavaBridge
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

/**
 * @author yudong
 */
class TimestampDateFunction(private val jsExecutor: JsExecutor) : HttpBaseFunction() {

    override fun callInner(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val day = JavaBridge.convertArg(args!![0])!!
        val parentPath = jsExecutor.parentPath

        val enum = InnerVariableEnum.TIMESTAMP_DATE
        return enum.exec(enum.methodName, parentPath, day)
    }

    override fun getArity(): Int {
        return 1
    }

}