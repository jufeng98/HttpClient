package org.javamaster.httpclient.js.support.req

import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
@Suppress("unused")
class RequestBody(
    private val jsBody: Any?,
    val rawBody: String?,
) {

    fun getRaw(): String? {
        return rawBody
    }

    fun tryGetSubstituted(): String? {
        if (jsBody == null) {
            return null
        }

        if (jsBody is String) {
            return jsBody
        } else {
            val bodyArray = jsBody as ByteArray
            return String(bodyArray, StandardCharsets.UTF_8)
        }
    }

    fun string(): String? {
        return tryGetSubstituted()
    }

    fun bytes(): ByteArray? {
        if (jsBody == null) {
            return null
        }

        return if (jsBody is String) {
            jsBody.toByteArray(StandardCharsets.UTF_8)
        } else {
            jsBody as ByteArray
        }
    }

    override fun toString(): String {
        return "RequestBody(jsBody=$jsBody, rawBody=$rawBody)"
    }
}