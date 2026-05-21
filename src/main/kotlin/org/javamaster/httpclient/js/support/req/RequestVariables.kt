package org.javamaster.httpclient.js.support.req

import com.google.common.collect.Maps
import org.javamaster.httpclient.js.support.GlobalLog
import org.javamaster.httpclient.js.support.jsObject.Console
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("unused")
class RequestVariables {
    val dataHolder: MutableMap<String, Any?> = Maps.newConcurrentMap()

    fun isEmpty(): Boolean {
        return dataHolder.size == 0
    }

    fun get(key: String): Any? {
        return dataHolder[key]
    }

    fun set(key: String, value: Any?) {
        var desc: String
        if (value != null) {
            val str = Console.convertParam(value)
            desc = if (str.length > 300) {
                str.substring(0, 300) + "...(${NlsBundle.nls("content.truncated")})"
            } else {
                str
            }
        } else {
            desc = "null"
        }
        dataHolder[key] = value
        GlobalLog.log(key + NlsBundle.nls("value.req.set") + desc)
    }

    fun clear(key: String) {
        dataHolder.remove(key)
    }

    fun clearAll() {
        dataHolder.clear()
    }

    override fun toString(): String {
        return "RequestVariables(dataHolder=$dataHolder)"
    }

}