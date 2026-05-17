package org.javamaster.httpclient.crypto

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author yudong
 */
@Suppress("unused")
class HmacInitializer(private val mac: Mac, private val algorithm: String) {

    fun withTextSecret(textSecret: String): MacDigestBuilder {
        return withTextSecret(textSecret, StandardCharsets.UTF_8.name())
    }

    fun withTextSecret(textSecret: String, encoding: String): MacDigestBuilder {
        val secretKey = SecretKeySpec(textSecret.toByteArray(Charset.forName(encoding)), algorithm)
        mac.init(secretKey)
        return MacDigestBuilder(mac)
    }

    fun withHexSecret(hexSecret: String): MacDigestBuilder {
        val secretKey = SecretKeySpec(Hex.decodeHex(hexSecret), algorithm)
        mac.init(secretKey)
        return MacDigestBuilder(mac)
    }

    fun withBase64Secret(base64Secret: String): MacDigestBuilder {
        return withBase64Secret(base64Secret, false)

    }

    fun withBase64Secret(base64Secret: String, urlSafe: Boolean): MacDigestBuilder {
        val base64 = Base64.builder().setUrlSafe(urlSafe).get()
        val secretKey = SecretKeySpec(base64.decode(base64Secret), algorithm)
        mac.init(secretKey)
        return MacDigestBuilder(mac)
    }
}
