package org.javamaster.httpclient.js.support.res

import org.javamaster.httpclient.js.support.jsObject.Cookie

/**
 * @author yudong
 */
@Suppress("unused")
class HttpClientResponse(
    val status: Int,
    val headers: ResponseHeaders,
    val body: Any,
    val cookies: List<Cookie>,
) {
    var contentType: ContentType? = null

    init {
        val value = headers.valueOf("Content-Type")
        if (value != null) {
            val split = value.split(';')
            val charset = if (split.size > 1) split[1].split("=")[1] else ""
            contentType = ContentType(split[0], charset)
        }
    }

    fun cookiesByName(name: String): List<Cookie> {
        return cookies.filter { it.name == name }
    }

    override fun toString(): String {
        return "HttpClientResponse(status=$status, \nheaders=$headers, \nbody=$body, \ncookies=$cookies, \ncontentType=$contentType)"
    }

}