package org.javamaster.httpclient.curl.support

import org.apache.http.entity.ContentType
import java.io.File
import java.nio.charset.StandardCharsets


class CurlFormData(formData: String) {
    val headers: MutableList<CurlRequest.KeyValuePair> = mutableListOf()
    lateinit var name: String
    var formContentType: ContentType = ContentType.WILDCARD

    var file: File? = null
    lateinit var content: String

    private var hasFileContent = false

    init {
        val split = formData.split(";")

        for ((index, s) in split.withIndex()) {
            val strings = s.split("=")

            if (index == 0) {
                name = strings[0]
                if (strings.size > 1) {
                    parseContent(strings[1])
                }
                continue
            }

            parseAdditionalOption(strings)
        }
    }

    fun hasFileContent(): Boolean {
        return hasFileContent
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
