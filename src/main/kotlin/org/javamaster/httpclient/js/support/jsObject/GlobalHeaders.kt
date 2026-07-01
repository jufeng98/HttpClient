package org.javamaster.httpclient.js.support.jsObject

import org.javamaster.httpclient.js.support.GlobalLog
import org.javamaster.httpclient.js.support.req.RequestHeader
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("unused")
object GlobalHeaders {
    val dataHolder: MultiValueMap<String, String?> = LinkedMultiValueMap()

    fun all(): List<RequestHeader> {
        return dataHolder.map {
            RequestHeader(it.key, it.value)
        }
    }

    fun findByName(name: String): RequestHeader? {
        return all().firstOrNull { it.name == name }
    }

    fun add(key: String, value: String?): Boolean {
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
        dataHolder.add(key, value)
        GlobalLog.log(key + NlsBundle.nls("value.global.header.add") + desc)
        return true
    }

    fun set(key: String, value: String?) {
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
        dataHolder.set(key, value)
        GlobalLog.log(key + NlsBundle.nls("value.global.header.set") + desc)
    }

    fun clear(key: String) {
        dataHolder.remove(key)
    }

    fun modify(key: String, newKey: String, value: String, newValue: String): Boolean {
        if (key != newKey) {
            clear(key)

            return add(newKey, newValue)
        } else {
            val values = dataHolder[key] ?: return false
            values.forEachIndexed { idx, tmpValue ->
                if (tmpValue == value) {
                    values[idx] = newValue
                    return true
                }
            }

            return false
        }

    }

    fun delete(key: String, value: String): Boolean {
        val values = dataHolder[key] ?: return false

        if (values.size == 1) {
            clear(key)
        }

        val iterator = values.iterator()
        while (iterator.hasNext()) {
            val tmpValue = iterator.next()
            if (tmpValue == value) {
                iterator.remove()
                return true
            }
        }

        return false
    }

    override fun toString(): String {
        return "GlobalHeaders(dataHolder=$dataHolder)"
    }

}
