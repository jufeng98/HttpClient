package org.javamaster.httpclient.curl.support

import org.apache.http.entity.ContentType
import org.javamaster.httpclient.curl.exception.CurlParseException.Companion.newInvalidFormDataException
import java.io.File
import java.nio.charset.StandardCharsets


class CurlFormData(formData: String) {
    val headers: MutableList<CurlRequest.KeyValuePair> = mutableListOf()
    var name: String
    var formContentType: ContentType = ContentType.WILDCARD

    var file: File? = null
    lateinit var content: String

    private var hasFileContent = false

    init {
        val equalSignPosition = formData.indexOf('=')
        if (equalSignPosition < 0) {
            throw newInvalidFormDataException(formData)
        }

        name = formData.substring(0, equalSignPosition)
        val semicolonPosition = formData.indexOf(";")
        val contentString = formData.substring(
            equalSignPosition + 1,
            if (semicolonPosition < 0) formData.length else semicolonPosition
        )

        parseContent(contentString)

        if (semicolonPosition >= 0) {
            parseAdditionalOption(formData.substring(semicolonPosition + 1))
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

    private fun parseAdditionalOption(additionalFormData: String) {
        val equalSignPosition = additionalFormData.indexOf('=')

        if (equalSignPosition < 0) return

        val additionalOptionKey = additionalFormData.substring(0, equalSignPosition)

        val additionalOptionValue = additionalFormData
            .substring(equalSignPosition + 1)
            .replace("^\"|\"$|^'|'$".toRegex(), "")

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
