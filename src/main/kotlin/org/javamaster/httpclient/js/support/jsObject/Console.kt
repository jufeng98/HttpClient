package org.javamaster.httpclient.js.support.jsObject

import org.javamaster.httpclient.js.support.GlobalLog
import org.javamaster.httpclient.utils.JsonUtils.gsonNotPretty
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeJavaMethod
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.Scriptable

/**
 * @author yudong
 */
@Suppress("unused")
object Console {

    fun log(vararg arguments: Any?) {
        val str = arguments.joinToString(" ") {
            if (it == null) {
                return@joinToString "null"
            }

            if (it is Scriptable) {
                if (it is NativeArray) {
                    "(${it.size}) " + gsonNotPretty.toJson(it)
                } else if (it is NativeObject) {
                    it.className + " {" + it.entries.joinToString(", ") {
                        "" + it.key + ": " +
                                if (it.value is NativeJavaMethod)
                                    "Fun"
                                else
                                    it.value
                    } + "}"
                } else {
                    gsonNotPretty.toJson(it)
                }
            } else {
                "" + it
            }
        }
        GlobalLog.log(str)
    }

}