package org.javamaster.httpclient.mock

import com.sun.net.httpserver.HttpServer
import org.javamaster.httpclient.mock.support.RequestHandler
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.resolve.VariableResolver
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.function.Consumer


/**
 * @author yudong
 */
class MockServer {
    lateinit var resConsumer: Consumer<String>

    fun startServerAsync(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: Map<String, String>,
    ): HttpServer {
        val server = HttpServer.create(InetSocketAddress(resolvePort(request)), 0)
        server.executor = Executors.newCachedThreadPool()

        val requestHandler = RequestHandler(resConsumer, request, variableResolver, paramMap)
        server.createContext("/", requestHandler)

        return server
    }

    private fun resolvePort(request: HttpRequest): Int {
        val httpPort = request.requestTarget?.port
        return if (httpPort != null) {
            val firstChild = httpPort.firstChild
            val portStr = HttpPsiUtils.getNextSiblingByType(firstChild, HttpTypes.PORT_SEGMENT, false)!!.text
            portStr.toInt()
        } else {
            80
        }
    }

}