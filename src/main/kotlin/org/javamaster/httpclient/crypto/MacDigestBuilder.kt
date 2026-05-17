package org.javamaster.httpclient.crypto

import io.ktor.utils.io.charsets.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
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

    fun digest(): Digest {
        val digest = mac.doFinal()
        return Digest(digest)
    }

}
