package org.javamaster.httpclient.doc.support

import com.google.gson.JsonObject
import com.intellij.openapi.util.text.StringUtil

class HttpHeaderDocumentation private constructor(
    val name: String,
    private val myRfc: String,
    private val myRfcTitle: String,
    private val description: String,
    @Suppress("unused") val isDeprecated: Boolean,
) {
    constructor(name: String) : this(name, "", "", "", false)

    fun generateDoc(): String? {
        if (StringUtil.isEmpty(description)) {
            return null
        }

        val sb = (StringBuilder()).append(description)
        if (StringUtil.isNotEmpty(myRfc) && StringUtil.isNotEmpty(myRfcTitle)) {
            sb.append("<br/><br/>")
            sb.append("<a href=\"").append(RFC_PREFIX).append(myRfc).append("\">").append(myRfcTitle).append("</a>")
        }

        val var2 = url
        sb.append("<br/><br/>")
        sb.append("<a href=\"").append(var2).append("\">").append(name).append("</a> by ")
        sb.append("<a href=\"").append(var2).append("\$history").append("\">").append("Mozilla Contributors")
            .append("</a>")
        sb.append(CC_LICENSE)

        return sb.toString()
    }

    val url: String
        get() = URL_PREFIX + name

    companion object {
        private const val CC_LICENSE =
            " is licensed under <a href=\"https://creativecommons.org/licenses/by-sa/2.5/\">CC-BY-SA 2.5</a>."
        private const val URL_PREFIX = "https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/"
        private const val RFC_PREFIX = "https://tools.ietf.org/html/rfc"

        fun read(obj: JsonObject): HttpHeaderDocumentation? {
            val value = getValue(obj, "name")
            if (StringUtil.isEmpty(value)) {
                return null
            }

            val title = getValue(obj, "rfc-title")
            val ref = getValue(obj, "rfc-ref")
            val descr = getValue(obj, "descr")

            val element = obj["obsolete"]
            val isDeprecated = element != null && element.isJsonPrimitive && element.asBoolean

            return HttpHeaderDocumentation(value, ref, title, descr, isDeprecated)
        }

        private fun getValue(obj: JsonObject, key: String): String {
            val element = obj[key]
            return if (element != null && element.isJsonPrimitive) element.asString else ""
        }
    }
}
