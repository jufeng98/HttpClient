package org.javamaster.httpclient.js.support

import org.javamaster.httpclient.map.LinkedMultiValueMap

@Suppress("unused")
class RequestHeaders(private val headers: LinkedMultiValueMap<String, String>) {

    fun all(): List<RequestHeader> {
        return headers.map {
            RequestHeader(it.key, it.value)
        }
    }

    fun findByName(name: String): RequestHeader? {
        return all().firstOrNull { it.name == name }
    }

    fun set(name: String, value: String) {
        headers.set(name, value)
    }

    fun add(name: String, value: String) {
        headers.add(name, value)
    }

}