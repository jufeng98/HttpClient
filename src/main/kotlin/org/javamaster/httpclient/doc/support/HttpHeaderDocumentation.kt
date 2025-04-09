package org.javamaster.httpclient.doc.support

import com.google.gson.JsonObject
import com.intellij.openapi.util.text.StringUtil

/**
 * @author yudong
 */
class HttpHeaderDocumentation private constructor(
    val name: String,
    private val myRfc: String,
    private val myRfcTitle: String,
    private val description: String,
    val isDeprecated: Boolean,
) {
    constructor(name: String) : this(name, "", "", "", false)

    val url = URL_PREFIX + name

    fun generateDoc(): String? {
        if (StringUtil.isEmpty(description)) {
            return null
        }

        val sb = StringBuilder().append(description)

        if (StringUtil.isNotEmpty(myRfc) && StringUtil.isNotEmpty(myRfcTitle)) {
            sb.append("<br/><br/>").append("<a href=\"").append(RFC_PREFIX).append(myRfc)
                .append("\">").append(myRfcTitle).append("</a>")
        }

        sb.append("<br/><br/>").append("<a href=\"").append(url).append("\">").append(name)
            .append("</a> by ").append("<a href=\"").append(url).append("\$history").append("\">")
            .append("Mozilla Contributors").append("</a>").append(CC_LICENSE)

        return sb.toString()
    }

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

            val obsolete = obj["obsolete"]
            val isDeprecated = obsolete != null && obsolete.isJsonPrimitive && obsolete.asBoolean

            return HttpHeaderDocumentation(value, ref, title, descr, isDeprecated)
        }

        private fun getValue(obj: JsonObject, key: String): String {
            val element = obj[key] ?: return ""

            if (!element.isJsonPrimitive) return ""

            return element.asString
        }
    }
}
