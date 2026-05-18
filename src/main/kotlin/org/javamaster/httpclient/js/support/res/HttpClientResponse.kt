package org.javamaster.httpclient.js.support.res

/**
 * @author yudong
 */
class HttpClientResponse(
    val status: Int,
    val headers: ResponseHeaders,
    val body: Any,
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

}