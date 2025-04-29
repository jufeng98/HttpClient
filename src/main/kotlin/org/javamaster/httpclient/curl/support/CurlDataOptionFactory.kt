package org.javamaster.httpclient.curl.support

import com.intellij.openapi.util.text.StringUtil
import java.nio.charset.StandardCharsets


object CurlDataOptionFactory {
    private const val DATA_URL_ENCODE = "data-urlencode"
    private const val DATA = "data"
    private const val DATA_ASCII = "data-ascii"
    private const val DATA_BINARY = "data-binary"
    private const val DATA_RAW = "data-raw"

    fun getCurlDataOption(optionName: String, data: String): CurlDataOption? {
        when (optionName) {
            DATA_URL_ENCODE -> return CurlStringDataOption(data, StandardCharsets.UTF_8.name())
            DATA, DATA_ASCII, DATA_BINARY -> {
                if (!data.startsWith("@")) {
                    return CurlStringDataOption(data)
                } else {
                    val fileName = data.substring(1)
                    if (StringUtil.isNotEmpty(fileName)) {
                        return CurlFileDataOption(fileName)
                    }
                }
                return CurlStringDataOption(data)
            }

            DATA_RAW -> return CurlStringDataOption(data)
            else -> return null
        }
    }

}
