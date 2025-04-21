package org.javamaster.httpclient.curl.support

import com.intellij.openapi.util.text.StringUtil


object CurlDataOptionFactory {

    fun getCurlDataOption(optionName: String, data: String): CurlDataOption? {
        when (optionName) {
            "data-urlencode" -> return CurlStringDataOption(data, "UTF-8")
            "data", "data-ascii", "data-binary" -> {
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

            "data-raw" -> return CurlStringDataOption(data)
            else -> return null
        }
    }

}
