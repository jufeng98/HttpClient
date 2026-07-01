package org.javamaster.httpclient.mock.support

import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver

/**
 * @author yudong
 */
interface MockWsServer {

    fun startServer(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: LinkedMultiValueMap<String, String>,
    )

    fun stopServer()

}