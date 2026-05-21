package org.javamaster.httpclient.js.support.res

/**
 * @author yudong
 */
class HttpClientRequestRes(private val url: String, private val jsBody: Any?) {

    fun url(): String {
        return url
    }

    fun body(): Any? {
        return jsBody
    }

    override fun toString(): String {
        return "HttpClientRequestRes(url='$url', jsBody=$jsBody)"
    }
}