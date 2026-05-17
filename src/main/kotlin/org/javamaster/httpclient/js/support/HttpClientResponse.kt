package org.javamaster.httpclient.js.support

class HttpClientResponse(val status: Int,
                         val headers: ResponseHeaders,
                         val body: Any) {

    val contentType = headers.valuesOf("Content-Type")

}