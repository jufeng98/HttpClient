package org.javamaster.httpclient.js.support.jsObject

import org.javamaster.httpclient.js.support.GlobalLog
import org.javamaster.httpclient.utils.JsonUtils.gsonNotPretty
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeJavaMethod
import org.mozilla.javascript.NativeObject

/**
 * @author yudong
 */
@Suppress("unused")
object Console {
    fun log(first: Any?) {
        GlobalLog.log(convertParam(first))
    }

    fun log(first: Any?, sec: Any?) {
        GlobalLog.log("${convertParam(first)} ${convertParam(sec)}")
    }

    fun log(first: Any?, sec: Any?, third: Any?, vararg arguments: Any?) {
        val str = arguments.joinToString(" ") { convertParam(it) }

        GlobalLog.log("${convertParam(first)} ${convertParam(sec)} ${convertParam(third)} $str")
    }

    fun convertParam(param: Any?): String {
        if (param == null) {
            return "null"
        }

        try {
            return if (param is NativeArray) {
                gsonNotPretty.toJson(param)
            } else if (param is NativeObject) {
                param.className + " {" + param.entries.joinToString(", ") {
                    "" + it.key + ": " +
                            if (it.value is NativeJavaMethod) {
                                "Fun"
                            } else {
                                it.value?.toString() ?: "null"
                            }
                } + "}"
            } else {
                param.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return param.toString()
        }
    }

}