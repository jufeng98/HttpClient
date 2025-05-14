package org.javamaster.httpclient.curl.data

import org.apache.http.entity.ContentType
import org.javamaster.httpclient.curl.support.CurlRequest
import java.io.File
import java.nio.charset.StandardCharsets


class CurlFormData(formData: String) {
    val headers: MutableList<CurlRequest.KeyValuePair> = mutableListOf()

    var formContentType: ContentType = ContentType.WILDCARD
    var file: File? = null

    lateinit var name: String
    lateinit var content: String

    var hasFileContent = false

    init {
        val parts = formData.split(";")

        for ((index, part) in parts.withIndex()) {
            val split = part.split("=")

            if (index == 0) {
                name = split[0]

                if (split.size > 1) {
                    parseContent(split[1])
                }

                continue
            }

            parseAdditionalOption(split)
        }
    }

    private fun parseContent(contentString: String) {
        if (contentString.startsWith("@")) {
            content = contentString.substring(1)

            hasFileContent = true

            file = File(content)
        } else {
            content = contentString

            hasFileContent = false
        }
    }

    private fun parseAdditionalOption(additionalFormData: List<String>) {
        val additionalOptionKey = additionalFormData[0]

        if (additionalFormData.size <= 1) {
            return
        }

        val additionalOptionValue = additionalFormData[1].replace("^\"|\"$|^'|'$".toRegex(), "")

        if (additionalOptionKey == "filename" && hasFileContent) {
            file = File(file!!.parent, additionalOptionValue)
        } else if (additionalOptionKey == "type") {
            formContentType = ContentType.create(additionalOptionValue, StandardCharsets.UTF_8)
        } else if (additionalOptionKey == "headers") {
            val colonPosition = additionalOptionValue.indexOf(':')
            if (colonPosition < 0) {
                headers.add(CurlRequest.KeyValuePair(additionalOptionValue, ""))
            } else {
                headers.add(
                    CurlRequest.KeyValuePair(
                        additionalOptionValue.substring(0, colonPosition),
                        additionalOptionValue.substring(colonPosition + 1).trim { it <= ' ' }
                    )
                )
            }
        }
    }
}
