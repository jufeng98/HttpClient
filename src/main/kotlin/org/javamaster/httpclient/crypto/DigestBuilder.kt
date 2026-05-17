package org.javamaster.httpclient.crypto

import io.ktor.utils.io.charsets.name
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

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

    fun digest(): Digest {
        val digest = messageDigest.digest()
        return Digest(digest)
    }

}
