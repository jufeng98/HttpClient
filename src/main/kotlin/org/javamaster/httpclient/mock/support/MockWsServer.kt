package org.javamaster.httpclient.mock.support

import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver

/**
 * @author yudong
 */
interface MockWsServer {

    fun startServer(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: MultiValueMap<String, String>,
    )

    fun stopServer()

}