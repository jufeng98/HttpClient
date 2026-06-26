package org.javamaster.httpclient.mock

import com.sun.net.httpserver.HttpServer
import org.javamaster.httpclient.mock.support.MockServerHelper
import org.javamaster.httpclient.mock.support.RequestHandler
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import java.net.InetSocketAddress
import java.util.concurrent.Executors


/**
 * @author yudong
 */
class MockServer(private val port: Int, private val httpDashboardForm: HttpDashboardForm) {
    private var httpServer: HttpServer? = null

    fun startServer(request: HttpRequest, variableResolver: VariableResolver, paramMap: Map<String, String>) {
        val requestHandler = RequestHandler(httpDashboardForm, request, variableResolver, paramMap)

        httpServer = HttpServer.create(InetSocketAddress(port), 0)
        httpServer!!.executor = Executors.newCachedThreadPool()

        httpServer!!.createContext("/", requestHandler)
        httpServer!!.start()

        mockServerRunningSet.add(port)

        httpDashboardForm.showMockServerLog(MockServerHelper.appendTime(NlsBundle.nls("mock.server.start", port) + "\n"))
    }

    fun stopServer() {
        mockServerRunningSet.remove(port)

        httpServer?.stop(0)

        httpDashboardForm.showMockServerLog(MockServerHelper.appendTime("Server stopped\n"))
    }

    companion object {
        private val mockServerRunningSet = mutableSetOf<Int>()

        fun isRunning(port: Int): Boolean {
            return mockServerRunningSet.contains(port)
        }
    }
}