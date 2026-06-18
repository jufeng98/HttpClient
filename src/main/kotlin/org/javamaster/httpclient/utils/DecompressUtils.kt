package org.javamaster.httpclient.utils

import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.util.zip.GZIPInputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

/**
 * @author yudong
 */
object DecompressUtils {

    fun decompressBodyBytes(bodyBytes: ByteArray, contentEncoding: String): ByteArray {
        var bytes = bodyBytes

        when (contentEncoding) {
            "gzip", "x-gzip" -> GZIPInputStream(ByteArrayInputStream(bodyBytes)).use {
                bytes = it.readAllBytes()
            }

            "deflate" -> bytes = decompressDeflate(bodyBytes)

            else -> throw UnsupportedEncodingException("Unknown Content-Encoding: $contentEncoding, only support deflate, gzip, x-gzip")
        }

        return bytes
    }

    fun decompressDeflate(compressedData: ByteArray): ByteArray {
        try {
            return InflaterInputStream(ByteArrayInputStream(compressedData), Inflater(false)).use {
                it.readAllBytes()
            }
        } catch (e: Exception) {
            logWarn("InflaterInputStream error", e)
        }

        try {
            // 1. 先尝试标准 zlib 格式（nowrap = false）
            val inflater = Inflater(false)
            inflater.setInput(compressedData)
            try {
                ByteArrayOutputStream().use {
                    val buffer = ByteArray(1024)
                    while (!inflater.finished()) {
                        val count = inflater.inflate(buffer)
                        it.write(buffer, 0, count)
                    }
                    return it.toByteArray()
                }
            } finally {
                inflater.end()
            }
        } catch (e: Exception) {
            logWarn("Inflater(false) error", e)
        }

        // 2. 如果报错（通常为 header 校验失败），尝试原始 deflate 数据（nowrap = true）
        val inflater = Inflater(true)
        inflater.setInput(compressedData)
        try {
            ByteArrayOutputStream().use {
                val buffer = ByteArray(1024)
                while (!inflater.finished()) {
                    val count = inflater.inflate(buffer)
                    it.write(buffer, 0, count)
                }
                return it.toByteArray()
            }
        } finally {
            inflater.end()
        }
    }

}