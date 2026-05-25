package org.javamaster.httpclient.crypto

import io.ktor.util.*
import java.util.Base64

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
        val bytes = if (urlSafe) {
            Base64.getUrlEncoder()
        } else {
            Base64.getEncoder()
        }
        return bytes.encodeToString(digest)
    }
}
