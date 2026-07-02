package org.javamaster.httpclient.mock

import com.sun.net.httpserver.HttpServer
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.mock.support.MockHttpRequestHandler
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.processHandler.MockHttpProcessHandler
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import java.net.InetSocketAddress
import java.util.concurrent.Executors


/**
 * @author yudong
 */
class MockHttpServer(
    private val port: Int,
    private val httpDashboardForm: HttpDashboardForm,
    private val handler: MockHttpProcessHandler,
) {
    private var httpServer: HttpServer? = null

    fun startServer(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: MultiValueMap<String, String>,
    ) {
        val mockHttpRequestHandler = MockHttpRequestHandler(httpDashboardForm, request, variableResolver, paramMap, handler)

        httpServer = HttpServer.create(InetSocketAddress(port), 0)
        httpServer!!.executor = Executors.newCachedThreadPool()

        httpServer!!.createContext("/", mockHttpRequestHandler)
        httpServer!!.start()

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.server.start", port) + "\n")
    }

    fun stopServer() {
        httpServer?.stop(0)

        httpDashboardForm.showMockServerLog("Http Server stopped\n")
    }

}