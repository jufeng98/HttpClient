package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
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

        runInEdt {
            try {
                loadingRemover?.run()

                httpDashboardForm.initMockServerForm()

                mockServer = MockServer(port, httpDashboardForm)

                mockServer!!.startServer(request, variableResolver, paramMap)

                NotifyUtil.notifyInfo(project, NlsBundle.nls("mock.server.start", port))

                ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.SERVICES)?.show()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override fun destroyProcessImpl() {
        mockServer?.stopServer()

        super.destroyProcessImpl()
    }
}