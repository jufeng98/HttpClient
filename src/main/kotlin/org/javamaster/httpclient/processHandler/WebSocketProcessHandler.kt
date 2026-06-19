package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.js.support.jsObject.GlobalHeaders
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.CookieUtils
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

        val reqInfo = createHttpReqInfo()

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(request.header?.headerFieldList, variableResolver)

        if (!paramMap.containsKey(ParamEnum.NO_COOKIE_JAR.param)) {
            CookieUtils.addFileCookieToReqHeader(url, reqHeaderMap, reqInfo.fileCookies)
        }

        reqHeaderMap.addAll(GlobalHeaders.dataHolder)

        wsRequest = WsRequest(url, reqHeaderMap, this, paramMap)

        httpDashboardForm.restoreInputHistoryList()

        runInEdt {
            try {
                loadingRemover?.run()

                httpDashboardForm.initWsForm(wsRequest)

                wsRequest!!.connect()

                ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.SERVICES)?.show()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override fun destroyProcessImpl() {
        wsRequest?.abortConnect()

        super.destroyProcessImpl()
    }

}