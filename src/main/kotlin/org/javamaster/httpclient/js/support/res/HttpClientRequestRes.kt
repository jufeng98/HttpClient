package org.javamaster.httpclient.js.support.res

import org.javamaster.httpclient.js.support.req.RequestHeaders
import org.javamaster.httpclient.js.support.req.RequestVariables
import org.javamaster.httpclient.processHandler.ProcessHandlerBase

/**
 * @author yudong
 */
@Suppress("unused")
class HttpClientRequestRes(
    private val url: String,
    private val jsBody: Any?,
    val method: String,
    val environment: MutableMap<String, String>,
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

    fun url(): String {
        return url
    }

    fun body(): Any? {
        return jsBody
    }

    override fun toString(): String {
        return "HttpClientRequestRes(url='$url', jsBody=$jsBody, environment=$environment, variables=$variables, fileVariables=$fileVariables, headers=$headers)"
    }

}