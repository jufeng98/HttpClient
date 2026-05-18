package org.javamaster.httpclient.js.support.jsObject

import org.javamaster.httpclient.exception.HttpFileException
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author yudong
 */
@Suppress("unused")
object Window {

    fun btoa(bytes: String): String {
        try {
            return Base64.getEncoder().encodeToString(bytes.toByteArray())
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

    fun atob(str: String): String {
        try {
            return String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw HttpFileException(e.toString(), e)
        }
    }

}