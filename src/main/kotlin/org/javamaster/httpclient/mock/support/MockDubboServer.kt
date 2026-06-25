package org.javamaster.httpclient.mock.support

import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver

/**
 * @author yudong
 */
interface MockDubboServer {

    fun startServer(request: HttpRequest, variableResolver: VariableResolver, paramMap: Map<String, String>)

    fun stopServer()

}