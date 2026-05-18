package org.javamaster.httpclient.js.support

import com.google.common.collect.Maps
import org.javamaster.httpclient.nls.NlsBundle.nls

@Suppress("unused")
object GlobalVariables {
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
            val str = "" + value
            desc = if (str.length > 300) {
                str.substring(0, 300) + "...(${nls("content.truncated")})"
            } else {
                str
            }
        } else {
            desc = "null"
        }
        dataHolder[key] = value
        GlobalLog.log(key + nls("value.global.set") + desc)
    }

    fun clear(key: String) {
        dataHolder.remove(key)
    }

    fun clearAll() {
        dataHolder.clear()
    }

}