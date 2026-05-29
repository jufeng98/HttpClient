package org.javamaster.httpclient.processHandler

import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.javamaster.httpclient.mock.MockServer
import org.javamaster.httpclient.mock.support.MockServerHelper
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * @author yudong
 */
class MockServerProcessHandler(httpMethod: HttpMethod, selectedEnv: String?) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var mockServer: MockServer? = null

    override fun startProcess() {
        val port = MockServerHelper.resolvePort(requestTarget.port)

        switchToEdt {
            loadingRemover?.run()

            val resConsumer = httpDashboardForm.initMockServerForm()

            mockServer = MockServer(resConsumer, port)

            mockServer!!.startServer(request, variableResolver, paramMap)

            NotifyUtil.notifyInfo(project, NlsBundle.nls("mock.server.start", port))

            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow(ToolWindowId.SERVICES)
            toolWindow?.show()
        }
    }

    override fun destroyProcessImpl() {
        mockServer?.stopServer()

        super.destroyProcessImpl()
    }
}