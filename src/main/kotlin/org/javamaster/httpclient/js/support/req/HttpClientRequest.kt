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
    val fileVariables: MutableMap<String, String>,
    val headers: RequestHeaders,
) {
    override fun toString(): String {
        return "HttpClientRequest(environment=$environment, \nurl=$url, \nbody=$body, \nmethod='$method', " +
                "\nvariables=$variables, \nfileVariables=$fileVariables, \nheaders=$headers)"
    }
}