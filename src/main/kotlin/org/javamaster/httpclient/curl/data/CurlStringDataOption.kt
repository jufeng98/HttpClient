package org.javamaster.httpclient.curl.data

import com.intellij.openapi.util.text.StringUtil
import org.javamaster.httpclient.curl.support.CurlRequest
import java.net.URLEncoder


class CurlStringDataOption : CurlDataOption {
    private val myData: String

    constructor(data: String) {
        myData = data
    }

    constructor(data: String, encoding: String) {
        myData = encodeData(data, encoding)
    }

    override fun apply(curlRequest: CurlRequest) {
        if (StringUtil.isNotEmpty(curlRequest.textToSend)) {
            curlRequest.textToSend = curlRequest.textToSend + "&" + myData
        } else {
            curlRequest.textToSend = myData
        }

        curlRequest.haveTextToSend = true
    }

    companion object {

        private fun encodeData(data: String, encoding: String): String {
            var content = data
            var name: String? = null

            if (data.contains("=")) {
                if (data.indexOf("=") == 0) {
                    content = data.substring(1)
                } else {
                    val nameContent = data.split("=", limit = 2)
                    name = nameContent[0]
                    content = nameContent[1]
                }
            }

            val encodedData = URLEncoder.encode(content, encoding)

            return if (name != null) "$name=$encodedData" else encodedData
        }
    }

}