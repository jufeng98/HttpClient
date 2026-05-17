package org.javamaster.httpclient.crypto

import javax.crypto.Mac

/**
 * @author yudong
 */
@Suppress("unused")
class HmacSupport {

    fun sha1(): HmacInitializer {
        return HmacInitializer(Mac.getInstance("HmacSHA1"), "HmacSHA1")
    }

    fun sha256(): HmacInitializer {
        return HmacInitializer(Mac.getInstance("HmacSHA256"), "HmacSHA256")
    }

    fun sha384(): HmacInitializer {
        return HmacInitializer(Mac.getInstance("HmacSHA384"), "HmacSHA384")
    }

    fun sha512(): HmacInitializer {
        return HmacInitializer(Mac.getInstance("HmacSHA512"), "HmacSHA512")
    }

    fun md5(): HmacInitializer {
        return HmacInitializer(Mac.getInstance("HmacMD5"), "HmacMD5")
    }

}
