package org.javamaster.httpclient.crypto

import io.ktor.util.*
import io.ktor.utils.io.charsets.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac

/**
 * @author yudong
 */
@Suppress("unused")
class MacDigestBuilder(private val mac: Mac) {

    fun updateWithText(textInput: String): MacDigestBuilder {
        updateWithText(textInput, StandardCharsets.UTF_8.name)
        return this
    }

    fun updateWithText(textInput: String, encoding: String): MacDigestBuilder {
        mac.update(textInput.toByteArray(Charset.forName(encoding)))
        return this
    }

    fun updateWithBase64(base64: String): MacDigestBuilder {
        updateWithBase64(base64, false)
        return this
    }

    fun updateWithBase64(base64: String, urlSafe: Boolean): MacDigestBuilder {
        val decoder = if (urlSafe) {
            Base64.getUrlDecoder()
        } else {
            Base64.getDecoder()
        }

        val bytes = decoder.decode(base64)

        mac.update(bytes)
        return this
    }

    fun updateWithHex(hexInput: String): MacDigestBuilder {
        val bytes = hex(hexInput)

        mac.update(bytes)
        return this
    }

    fun digest(): Digest {
        val digest = mac.doFinal()
        return Digest(digest)
    }

}
