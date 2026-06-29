package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.javamaster.httpclient.mock.MockWsServerImpl
import org.javamaster.httpclient.mock.support.MockWsServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * @author yudong
 */
class MockWsProcessHandler(httpMethod: HttpMethod, selectedEnv: String?, private val port: Int) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var mockWsServer: MockWsServer? = null

    override fun startProcess() {
        mockServerRunningSet.add(port)

        val path = computeReadAction { resolvePath(request, variableResolver) }

        runInEdt {
            try {
                loadingRemover?.run()

                httpDashboardForm.initMockServerForm()

                mockWsServer = MockWsServerImpl(port, path, httpDashboardForm)

                mockWsServer!!.startServer(request, variableResolver, paramMap)

                NotifyUtil.notifyInfo(project, NlsBundle.nls("mock.ws.server.start", port))

                ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.SERVICES)?.show()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override fun destroyProcessImpl() {
        mockServerRunningSet.remove(port)

        mockWsServer?.stopServer()

        super.destroyProcessImpl()
    }

    private fun resolvePath(request: HttpRequest, variableResolver: VariableResolver): String {
        val pathAbsolute = request.requestTarget!!.pathAbsolute
        return if (pathAbsolute != null) {
            variableResolver.resolve(pathAbsolute.text)
        } else {
            "/"
        }
    }

    companion object {
        private val mockServerRunningSet = mutableSetOf<Int>()

        fun isRunning(port: Int): Boolean {
            return mockServerRunningSet.contains(port)
        }
    }
}