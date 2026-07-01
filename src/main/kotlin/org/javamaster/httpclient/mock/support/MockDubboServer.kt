package org.javamaster.httpclient.mock.support

import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import java.util.concurrent.CompletableFuture

/**
 * @author yudong
 */
interface MockDubboServer {

    fun startServerAsync(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: LinkedMultiValueMap<String, String>,
    ): CompletableFuture<Void>

    fun stopServer()

}