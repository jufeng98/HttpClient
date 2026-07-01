package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.javamaster.httpclient.logger.HttpRequestLogger
import org.javamaster.httpclient.mock.MockServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.DocUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * @author yudong
 */
class MockServerProcessHandler(httpMethod: HttpMethod, selectedEnv: String?, private val port: Int) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var mockServer: MockServer? = null

    override fun startProcess() {
        mockServerRunningSet.add(port)

        var url = resolveAndHandleUrl()

        val reqInfo = createHttpReqInfo()

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(request.header?.headerFieldList, variableResolver)

        val preJsResList = executePreJs(url, reqInfo, reqHeaderMap)
        HttpRequestLogger.logInfo("前置 js 执行结果:$preJsResList")

        val pair = DocUtils.createMockDoc(tabName, project)

        runInEdt {
            try {
                loadingRemover?.run()

                httpDashboardForm.initMockServerForm(pair)

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
        mockServerRunningSet.remove(port)

        mockServer?.stopServer()

        super.destroyProcessImpl()
    }

    companion object {
        private val mockServerRunningSet = mutableSetOf<Int>()

        fun isRunning(port: Int): Boolean {
            return mockServerRunningSet.contains(port)
        }
    }

}