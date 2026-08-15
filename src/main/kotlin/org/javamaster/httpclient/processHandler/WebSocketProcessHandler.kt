package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.js.support.jsObject.GlobalHeaders
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.CookieUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.ReqUtils
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

        var reqHeaderMap: MultiValueMap<String, String?> =
            HttpUtils.convertToReqHeaderMap(request.header?.headerFieldList, variableResolver)

        if (!paramMap.containsKey(ParamEnum.NO_COOKIE_JAR.param)) {
            CookieUtils.addFileCookieToReqHeader(url, reqHeaderMap, reqInfo.fileCookies)
        }

        val reqBody = ReqUtils.resolveReqBodyAgain(reqInfo.reqBody, variableResolver, paramMap)

        reqHeaderMap.addAll(GlobalHeaders.dataHolder)

        httpDashboardForm.restoreInputHistoryList()

        runInEdt {
            try {
                loadingRemover?.run()

                var body = ""
                if (reqBody is Triple<*, *, *>) {
                    body = reqBody.second?.toString() ?: ""
                }

                val wsDashboardForm = httpDashboardForm.initWsForm(body)

                wsRequest = WsRequest(url, reqHeaderMap, this, paramMap, wsDashboardForm)

                wsRequest!!.connectAsync()

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