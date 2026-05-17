package org.javamaster.httpclient.js.support

import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.map.MultiValueMap
import java.io.Serializable
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
@Suppress("unused")
class URLSearchParams(queryParams: String?) {
    constructor() : this(null)

    private val params: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>()

    init {
        queryParams?.split("&")
            ?.forEach { pair ->
                val split = pair.split('=')
                val key = split[0]
                val value = split[1]
                params[URLDecoder.decode(key, StandardCharsets.UTF_8)] =
                    URLDecoder.decode(value, StandardCharsets.UTF_8)
            }
    }

    fun append(key: String, value: String) {
        params.add(key, value)
    }

    fun has(key: String): Boolean {
        return params.containsKey(key)
    }

    fun get(key: String): String? {
        val values = params[key]
        if (values == null || values.isEmpty()) {
            return null
        }

        return values[0]
    }

    fun set(key: String, value: String) {
        params[key] = value
    }

    fun getClassName(): String {
        return this::class.java.name
    }

    fun delete(key: String) {
        params.remove(key)
    }

    fun values(): List<String> {
        return params.values.flatten()
    }

    fun entries(): Array<Array<Serializable>> {
        return params.entries
            .map {
                val key = it.key
                val value = it.value.toTypedArray()
                arrayOf(key, value)
            }
            .toTypedArray()
    }

    override fun toString(): String {
        return params.entries
            .joinToString("&") {
                val key = it.key
                val value = it.value
                value.joinToString("&") { innerIt ->
                    URLEncoder.encode(key, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(
                        innerIt,
                        StandardCharsets.UTF_8
                    )
                }
            }
    }
}