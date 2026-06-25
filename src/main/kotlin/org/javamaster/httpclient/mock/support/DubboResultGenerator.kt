package org.javamaster.httpclient.mock.support

import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.ReqUtils

/**
 * @author yudong
 */
object DubboResultGenerator {

    fun generate(request: HttpRequest, variableResolver: VariableResolver): String {
        val pair = MockServerHelper.computeResBody(request, variableResolver)

        val convertReqBody = ReqUtils.convertReqBody(pair.first) ?: return """
            {
                "error":"no response body"
            }
        """.trimIndent()

        if (convertReqBody !is String) {
            return """
            {
                "error":"response body not string"
            }
        """.trimIndent()
        }

        return convertReqBody
    }

}