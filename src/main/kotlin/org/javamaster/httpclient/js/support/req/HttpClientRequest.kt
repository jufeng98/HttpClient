package org.javamaster.httpclient.js.support.req

/**
 * @author yudong
 */
@Suppress("unused")
class HttpClientRequest(
    val environment: MutableMap<String, String>,
    val url: RequestUrl,
    val body: RequestBody,
    val method: String,
    val variables: RequestVariables,
    val globalVariables: MutableMap<String, String>,
    val headers: RequestHeaders,
)