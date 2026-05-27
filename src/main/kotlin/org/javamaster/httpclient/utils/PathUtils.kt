package org.javamaster.httpclient.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
object PathUtils {
    private val reservedNameChars = setOf('<', '>', ':', '"', '/', '\\', '|', '?')
    private val reservedPathChars = setOf('<', '>', '"', '|', '?')

    fun legalizeFileName(name: String): String {
        return name.toCharArray()
            .map {
                if (it == '*') {
                    "%2A"
                } else if (reservedNameChars.contains(it)) {
                    URLEncoder.encode(it.toString(), StandardCharsets.UTF_8)
                } else {
                    it
                }
            }
            .joinToString("")
    }

    fun legalizeFilePath(name: String): String {
        return name.toCharArray()
            .map {
                if (it == '*') {
                    "%2A"
                } else if (reservedPathChars.contains(it)) {
                    URLEncoder.encode(it.toString(), StandardCharsets.UTF_8)
                } else {
                    it
                }
            }
            .joinToString("")
    }
}