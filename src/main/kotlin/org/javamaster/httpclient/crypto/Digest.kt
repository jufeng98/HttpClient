package org.javamaster.httpclient.crypto

import io.ktor.util.*
import org.apache.commons.codec.binary.Base64

/**
 * @author yudong
 */
@Suppress("unused")
class Digest(private val digest: ByteArray) {

    fun toHex(): String {
        return hex(digest).toString()
    }

    fun toBase64(): String {
        return toBase64(false)
    }

    fun toBase64(urlSafe: Boolean): String {
        val base64 = Base64.builder().setUrlSafe(urlSafe).get()
        return base64.encodeAsString(digest)
    }
}
