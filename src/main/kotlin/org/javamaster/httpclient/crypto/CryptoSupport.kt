package org.javamaster.httpclient.crypto

import com.intellij.util.io.DigestUtil
import java.security.MessageDigest
import java.security.Security

/**
 * @author yudong
 */
@Suppress("unused")
object CryptoSupport {

    val hmac = HmacSupport()

    fun sha1(): DigestBuilder {
        return DigestBuilder(DigestUtil.sha1())
    }

    fun sha256(): DigestBuilder {
        return DigestBuilder(DigestUtil.sha256())
    }

    fun sha384(): DigestBuilder {
        val messageDigest = MessageDigest.getInstance("SHA-384", Security.getProvider("SUN"))
        return DigestBuilder(messageDigest)
    }

    fun sha512(): DigestBuilder {
        return DigestBuilder(DigestUtil.sha512())
    }

    fun md5(): DigestBuilder {
        return DigestBuilder(DigestUtil.md5())
    }

}
