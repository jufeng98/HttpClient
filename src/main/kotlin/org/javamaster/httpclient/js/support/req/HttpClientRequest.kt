package org.javamaster.httpclient.js.support.req

import org.javamaster.httpclient.processHandler.ProcessHandlerBase

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

    fun iteration(): Int? {
        val arrayIndex = ProcessHandlerBase.repeatExecutor?.arrayIndex
        if (arrayIndex == null) {
            return null
        }

        return arrayIndex - 1
    }

    fun templateValue(expressionNumber: Int): Any? {
        if (expressionNumber < ProcessHandlerBase.templateValues.size) {
            return ProcessHandlerBase.templateValues[expressionNumber]
        }

        return null
    }

    override fun toString(): String {
        return "HttpClientRequest(environment=$environment, \nurl=$url, \nbody=$body, \nmethod='$method', " +
                "\nvariables=$variables, \nfileVariables=$fileVariables, \nheaders=$headers)"
    }
}