package org.javamaster.httpclient.js.support.req

import org.javamaster.httpclient.js.support.GlobalLog
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("unused")
class RequestHeaders(private val headers: MultiValueMap<String, String?>) {

    fun all(): List<RequestHeader> {
        return headers.map {
            RequestHeader(it.key, it.value)
        }
    }

    fun findByName(name: String): RequestHeader? {
        return all().firstOrNull { it.name == name }
    }

    fun set(name: String, value: String?) {
        headers.set(name, value)

        GlobalLog.log(NlsBundle.nls("req.header.set", name, value ?: "null"))
    }

    fun add(name: String, value: String?) {
        headers.add(name, value)

        GlobalLog.log(NlsBundle.nls("req.header.add", name, value ?: "null"))
    }

    override fun toString(): String {
        return "RequestHeaders(headers=$headers)"
    }

}