package org.javamaster.httpclient.js.support.func

import com.jayway.jsonpath.JsonPath
import org.mozilla.javascript.BaseFunction
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

/**
 * @author yudong
 */
@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object JsonPathFunction : BaseFunction() {

    override fun call(cx: Context?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any?>?): Any? {
        val obj = args!![0]
        val expression = args[1] as String
        return JsonPath.read(obj, expression)
    }

    override fun getArity(): Int {
        return 2
    }

}