package org.javamaster.httpclient.mock

import com.sun.net.httpserver.HttpServer
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.mock.support.RequestHandler
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.processHandler.MockServerProcessHandler
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import java.net.InetSocketAddress
import java.util.concurrent.Executors


/**
 * @author yudong
 */
class MockServer(
    private val port: Int,
    private val httpDashboardForm: HttpDashboardForm,
    private val handler: MockServerProcessHandler,
) {
    private var httpServer: HttpServer? = null

    fun startServer(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: MultiValueMap<String, String>,
    ) {
        val requestHandler = RequestHandler(httpDashboardForm, request, variableResolver, paramMap, handler)

        httpServer = HttpServer.create(InetSocketAddress(port), 0)
        httpServer!!.executor = Executors.newCachedThreadPool()

        httpServer!!.createContext("/", requestHandler)
        httpServer!!.start()

        httpDashboardForm.showMockServerLog(NlsBundle.nls("mock.server.start", port) + "\n")
    }

    fun stopServer() {
        httpServer?.stop(0)

        httpDashboardForm.showMockServerLog("Http Server stopped\n")
    }

}