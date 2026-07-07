package org.javamaster.httpclient.processHandler

import com.google.common.net.HttpHeaders
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts.Companion.RES_SIZE_LIMIT
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.exception.JsScriptException
import org.javamaster.httpclient.js.support.JsExecuteResult
import org.javamaster.httpclient.js.support.jsObject.GlobalHeaders
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.*
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull


/**
 * @author yudong
 */
class HttpProcessHandler(httpMethod: HttpMethod, selectedEnv: String?) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var redirectTimes = 0

    override fun startProcess() {
        requestRunningSet.add(tabName)

        var url = resolveAndHandleUrl()

        runInEdt { httpDashboardForm.initLabelLoading(tabName, url) }

        val reqInfo = createHttpReqInfo()

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(request.header?.headerFieldList, variableResolver)

        val preJsResList = executePreJs(url, reqInfo, reqHeaderMap)

        val jsBeforeExecuteResult = JsExecuteResult(preJsResList, jsScriptException)

        // 由于 前置 js 处理器有可能新增或修改了变量,所以 url、header、body 都需要重新解析一遍
        url = variableResolver.resolve(url)

        if (paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)) {
            url = ReqUtils.encodeUrl(url)
        }

        if (!paramMap.containsKey(ParamEnum.NO_COOKIE_JAR.param)) {
            CookieUtils.addFileCookieToReqHeader(url, reqHeaderMap, reqInfo.fileCookies)
        }

        reqHeaderMap.addAll(GlobalHeaders.dataHolder)

        reqHeaderMap = HttpUtils.resolveReqHeaderMapAgain(reqHeaderMap, variableResolver)

        val reqBody = ReqUtils.resolveReqBodyAgain(reqInfo.reqBody, variableResolver, paramMap)

        val httpReqDescList = mutableListOf<String>()

        if (methodType == HttpRequestEnum.GET) {
            val future = ReqUtils.getContentLength(url, version, reqHeaderMap, paramMap)

            this.future = future

            future.whenCompleteAsync { response, throwable ->
                var length = if (throwable != null) {
                    logWarn("获取长度错误", throwable)
                    -1
                } else {
                    val length = response.headers().firstValue(HttpHeaders.CONTENT_LENGTH).getOrNull()
                    length?.toInt() ?: -1
                }

                handleHttp(url, reqHeaderMap, reqBody, httpReqDescList, jsBeforeExecuteResult, length)
            }
        } else {
            handleHttp(url, reqHeaderMap, reqBody, httpReqDescList, jsBeforeExecuteResult, -1)
        }
    }

    private fun handleHttp(
        url: String,
        reqHeaderMap: MultiValueMap<String, String?>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
        jsBeforeExecuteResult: JsExecuteResult?,
        resContentLength: Int,
    ) {
        val targetMethodType = if (redirectTimes > 0) HttpRequestEnum.GET else methodType

        val pair = targetMethodType.preExecute(url, version, reqHeaderMap, reqBody, httpReqDescList, tabName, paramMap)

        if (jsScriptException != null) {
            dealPreJsErrorResponse(httpReqDescList, jsBeforeExecuteResult)

            return
        }

        val req = pair.first
        val reqContentLength = pair.second

        val start = System.currentTimeMillis()

        var elapseTime = 0
        elapseTimeFuture = ExecutorUtils.scheduledExecutor.scheduleAtFixedRate(Runnable {
            runInEdt { httpDashboardForm.updateLabelLoading(++elapseTime) }
        }, 1, 1, TimeUnit.SECONDS)

        runInEdt { httpDashboardForm.initProgress(resContentLength) }

        val future = targetMethodType.execute(paramMap, req) {
            runInEdt { httpDashboardForm.updateProgress(it, resContentLength) }
        }

        this.future = future

        future.whenCompleteAsync { response, throwable ->
            costTimes = System.currentTimeMillis() - start
            httpStatus = response?.statusCode()
            hasReqError = throwable != null

            application.executeOnPooledThread {
                if (ResUtils.shouldRedirect(httpStatus, paramMap)) {
                    redirectTimes++
                    if (redirectTimes > 3) {
                        detachProcess()

                        return@executeOnPooledThread
                    }

                    httpReqDescList.add("${CR_LF}// ${NlsBundle.nls("redirect.times.req", redirectTimes)}${CR_LF}")

                    val locationUrl = ResUtils.resolveLocationUrl(url, response.headers())

                    handleHttp(locationUrl, LinkedMultiValueMap(), null, httpReqDescList, null, -1)

                    return@executeOnPooledThread
                }

                try {
                    if (hasReqError) {
                        val httpInfo = HttpInfo(httpReqDescList, mutableListOf(), null, null, throwable)
                        httpInfo.reqContentLength = reqContentLength

                        dealResponse(httpInfo)

                        detachProcess()

                        return@executeOnPooledThread
                    }

                    val resHeaders = response.headers()

                    val cookies = CookieUtils.parseAll(url, resHeaders)

                    var cookieSavePair: Pair<String, VirtualFile>? = null
                    if (!paramMap.containsKey(ParamEnum.NO_COOKIE_JAR.param)) {
                        cookieSavePair = CookieUtils.saveCookiesToFile(cookies, project)
                    }

                    val resHeaderList = ResUtils.convertResponseHeaders(resHeaders)

                    val resBody = response.body()
                    val httpResInfo = ResUtils.convertResponseBody(resBody, resHeaders)

                    val statusCode = response.statusCode()

                    var resList: List<String>
                    try {
                        resList = jsExecutor.evalJsAfterRequest(
                            url, reqBody, jsAfterReq, httpResInfo, statusCode,
                            resHeaders.map(), cookies, httpFile.name, httpDocument
                        )
                    } catch (e: JsScriptException) {
                        jsScriptException = e

                        resList = e.list
                    }

                    val jsAfterExecuteResult = JsExecuteResult(resList, jsScriptException)

                    val httpResDescList = mutableListOf<String>()

                    val versionDesc = MyPsiUtils.Companion.getVersionDesc(response.version())

                    val commentTabName = "### $tabName${CR_LF}"
                    httpResDescList.add(commentTabName)

                    if (paramMap.containsKey(ParamEnum.VISUALIZE_TIMESTAMP.param)) {
                        httpResDescList.add("# @${ParamEnum.VISUALIZE_TIMESTAMP.param}${CR_LF}")
                    }

                    httpResDescList.add(methodType.name + " " + response.uri() + " " + versionDesc + CR_LF)

                    httpResDescList.addAll(resHeaderList)

                    val simpleTypeEnum = httpResInfo.simpleTypeEnum
                    val bodyBytes = httpResInfo.bodyBytes
                    val bodyStr = httpResInfo.bodyStr
                    val contentType = httpResInfo.contentType

                    if (simpleTypeEnum.binary) {
                        val size = Formats.formatFileSize(resBody.size.toLong())
                        httpResDescList.add(NlsBundle.nls("res.binary.data", size))
                    } else {
                        if (bodyStr!!.length > RES_SIZE_LIMIT) {
                            httpResDescList.add("")
                        } else {
                            httpResDescList.add(bodyStr)
                        }
                    }

                    val httpInfo = HttpInfo(
                        httpReqDescList, httpResDescList, simpleTypeEnum, bodyBytes,
                        null, contentType, resHeaders, resolveOutputFilePath(), cookieSavePair, statusCode,
                        costTimes, resBody.size, jsBeforeExecuteResult, jsAfterExecuteResult, reqContentLength
                    )

                    dealResponse(httpInfo)

                    detachProcess()
                } catch (e: Exception) {
                    handleException(e)
                }
            }
        }
    }

    override fun destroyProcessImpl() {
        runInEdt { loadingRemover?.run() }

        requestRunningSet.remove(tabName)

        super.destroyProcessImpl()
    }
}