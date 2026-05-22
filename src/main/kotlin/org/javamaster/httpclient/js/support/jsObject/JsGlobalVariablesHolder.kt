package org.javamaster.httpclient.js.support.jsObject

import com.google.common.collect.Maps
import org.javamaster.httpclient.js.support.GlobalLog
import org.javamaster.httpclient.nls.NlsBundle

@Suppress("unused")
object JsGlobalVariablesHolder {
    val dataHolder: MutableMap<String, Any?> = Maps.newConcurrentMap()
    val headers: GlobalHeaders = GlobalHeaders

    fun isEmpty(): Boolean {
        return dataHolder.size == 0
    }

    fun get(key: String): Any? {
        return dataHolder[key]
    }

    fun set(key: String, value: Any?): Boolean {
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
        GlobalLog.log(key + NlsBundle.nls("value.global.set") + desc)
        return true
    }

    fun modify(key: String, newKey: String, newValue: String): Boolean {
        if (key != newKey) {
            clear(key)
        }

        return set(newKey, newValue)
    }

    fun clear(key: String): Boolean {
        dataHolder.remove(key)
        return true
    }

    fun getJsGlobalVariables(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        dataHolder.forEach {
            map[it.key] = Console.convertParamToValidJson(it.value)
        }

        return map
    }

    fun clearAll() {
        dataHolder.clear()
    }

    override fun toString(): String {
        return "GlobalVariables(dataHolder=$dataHolder)"
    }

}