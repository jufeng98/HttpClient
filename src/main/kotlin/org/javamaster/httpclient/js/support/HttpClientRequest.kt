package org.javamaster.httpclient.js.support

@Suppress("unused")
class HttpClientRequest(
    val environment: MutableMap<String, String>,
    val url: RequestUrl,
    val body: RequestBody,
    val method: String,
    val globalVariables: MutableMap<String, String>,
    val requestHeaders: RequestHeaders,
)