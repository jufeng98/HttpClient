package org.javamaster.httpclient.js.support

class HttpClientRequestRes(private val url: String, private val jsBody: Any?) {

    fun url(): String {
        return url
    }

    fun body(): Any? {
        return jsBody
    }
}