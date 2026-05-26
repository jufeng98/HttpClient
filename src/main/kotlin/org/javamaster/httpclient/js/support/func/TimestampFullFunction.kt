package org.javamaster.httpclient.js.support.func

import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.jsObject.JavaBridge
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

/**
 * @author yudong
 */
class TimestampFullFunction(private val jsExecutor: JsExecutor) : HttpBaseFunction() {

    override fun callInner(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val day = JavaBridge.convertArg(args!![0])!!
        val hour = JavaBridge.convertArg(args[1])!!
        val sec = JavaBridge.convertArg(args[2])!!
        val parentPath = jsExecutor.httpFile.virtualFile.parent.path

        val enum = InnerVariableEnum.TIMESTAMP_FULL
        return enum.exec(enum.methodName, parentPath, day, hour, sec)
    }

    override fun getArity(): Int {
        return 3
    }

}