package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.ws.WsRequest

/**
 * @author yudong
 */
class WebSocketProcessHandler(httpMethod: HttpMethod, selectedEnv: String?) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var wsRequest: WsRequest? = null

    override fun startProcess() {
        var url = resolveAndHandleUrl()

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(request.header?.headerFieldList, variableResolver)

        wsRequest = WsRequest(url, reqHeaderMap, this, paramMap)

        httpDashboardForm.restoreInputHistoryList()

        runInEdt {
            loadingRemover?.run()

            httpDashboardForm.initWsForm(wsRequest)

            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow(ToolWindowId.SERVICES)
            toolWindow?.show()
        }

        wsRequest!!.connect()
    }

    override fun destroyProcessImpl() {
        wsRequest?.abortConnect()

        super.destroyProcessImpl()
    }

}