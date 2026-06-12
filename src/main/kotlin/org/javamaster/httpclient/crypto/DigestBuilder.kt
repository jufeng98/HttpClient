package org.javamaster.httpclient.crypto

import io.ktor.util.hex
import io.ktor.utils.io.charsets.name
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

/**
 * @author yudong
 */
@Suppress("unused")
class DigestBuilder(private val messageDigest: MessageDigest) {

    fun updateWithText(textInput: String): DigestBuilder {
        updateWithText(textInput, StandardCharsets.UTF_8.name)
        return this
    }

    fun updateWithText(textInput: String, encoding: String): DigestBuilder {
        messageDigest.update(textInput.toByteArray(Charset.forName(encoding)))
        return this
    }

    fun updateWithBase64(base64: String): DigestBuilder {
        updateWithBase64(base64, false)
        return this
    }

    fun updateWithBase64(base64: String, urlSafe: Boolean): DigestBuilder {
        val decoder = if (urlSafe) {
            Base64.getUrlDecoder()
        } else {
            Base64.getDecoder()
        }

        val bytes = decoder.decode(base64)

        messageDigest.update(bytes)
        return this
    }

    fun updateWithHex(hexInput: String): DigestBuilder {
        val bytes = hex(hexInput)

        messageDigest.update(bytes)
        return this
    }

    fun digest(): Digest {
        val digest = messageDigest.digest()
        return Digest(digest)
    }

}
