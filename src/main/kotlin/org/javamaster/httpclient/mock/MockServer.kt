package org.javamaster.httpclient.mock

import com.sun.net.httpserver.HttpServer
import org.javamaster.httpclient.mock.support.MockServerHelper
import org.javamaster.httpclient.mock.support.RequestHandler
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.function.Consumer


/**
 * @author yudong
 */
class MockServer(private val resConsumer: Consumer<String>, private val port: Int) {
    private var httpServer: HttpServer? = null

    fun startServer(request: HttpRequest, variableResolver: VariableResolver, paramMap: Map<String, String>) {
        val requestHandler = RequestHandler(resConsumer, request, variableResolver, paramMap)

        httpServer = HttpServer.create(InetSocketAddress(port), 0)
        httpServer!!.executor = Executors.newCachedThreadPool()

        httpServer!!.createContext("/", requestHandler)
        httpServer!!.start()

        mockServerRunningSet.add(port)

        resConsumer.accept(MockServerHelper.appendTime(NlsBundle.nls("mock.server.start", port) + "\n"))
    }

    fun stopServer() {
        mockServerRunningSet.remove(port)

        httpServer?.stop(0)

        resConsumer.accept(MockServerHelper.appendTime("Server stopped\n"))
    }

    companion object {
        private val mockServerRunningSet = mutableSetOf<Int>()

        fun isRunning(port: Int): Boolean {
            return mockServerRunningSet.contains(port)
        }
    }
}