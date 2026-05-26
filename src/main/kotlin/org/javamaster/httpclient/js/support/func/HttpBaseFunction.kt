package org.javamaster.httpclient.js.support.func

import org.javamaster.httpclient.exception.JsFileException
import org.javamaster.httpclient.nls.NlsBundle
import org.mozilla.javascript.BaseFunction
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.WrappedException

/**
 * @author yudong
 */
abstract class HttpBaseFunction : BaseFunction() {

    override fun call(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        try {
            if (arity > 0 && args!!.size < arity) {
                throw IllegalArgumentException(NlsBundle.nls("param.error"))
            }

            return callInner(cx, scope, thisObj, args)
        } catch (e: Exception) {
            throw WrappedException(JsFileException("", e))
        }
    }

    abstract fun callInner(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any?

}
