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

    fun log(first: Any?, vararg arguments: Any?) {
        val str = arguments.joinToString(" ") {
            if (it == null) {
                return@joinToString "null"
            }

            try {
                if (it is Scriptable) {
                    if (it is NativeArray) {
                        "(${it.size}) " + gsonNotPretty.toJson(it)
                    } else if (it is NativeObject) {
                        it.className + " {" + it.entries.joinToString(", ") {
                            "" + it.key + ": " +
                                    if (it.value is NativeJavaMethod) {
                                        "Fun"
                                    } else {
                                    }
                            it.value?.toString() ?: "null"
                        } + "}"
                    } else {
                        gsonNotPretty.toJson(it)
                    }
                } else {
                    "" + it
                }
            } catch (e: Exception) {
                e.printStackTrace()
                it.toString()
            }
        }

        GlobalLog.log("$first $str")
    }

}