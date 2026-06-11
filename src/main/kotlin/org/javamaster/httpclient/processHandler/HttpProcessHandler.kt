package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.util.text.Formats
import com.intellij.util.application
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.js.support.jsObject.GlobalHeaders
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.*
import org.javamaster.httpclient.utils.HttpUtils.CR_LF

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

        url = variableResolver.resolve(url)

        val httpReqDescList = mutableListOf<String>()
        httpReqDescList.addAll(preJsResList)

        reqHeaderMap = HttpUtils.resolveReqHeaderMapAgain(reqHeaderMap, variableResolver)

        if (paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)) {
            url = ReqUtils.Companion.encodeUrl(url)
        }

        val reqBody = ReqUtils.Companion.resolveReqBodyAgain(reqInfo.reqBody, variableResolver)

        CookieUtils.addFileCookieToReqHeader(url, reqHeaderMap, reqInfo.fileCookies)

        reqHeaderMap.addAll(GlobalHeaders.dataHolder)

        handleHttp(url, reqHeaderMap, reqBody, httpReqDescList)
    }

    private fun handleHttp(
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
    ) {
        val methodTypeTmp = if (redirectTimes > 0) HttpRequestEnum.GET else methodType

        val req = methodTypeTmp.preExecute(url, version, reqHeaderMap, reqBody, httpReqDescList, tabName, paramMap)

        val start = System.currentTimeMillis()

        val future = methodTypeTmp.execute(paramMap, req)

        this.future = future

        future.whenCompleteAsync { response, throwable ->
            costTimes = System.currentTimeMillis() - start
            httpStatus = response?.statusCode()
            hasError = throwable != null

            application.executeOnPooledThread {
                if (ResUtils.shouldRedirect(httpStatus, paramMap)) {
                    redirectTimes++
                    if (redirectTimes > 3) {
                        detachProcess()

                        return@executeOnPooledThread
                    }

                    httpReqDescList.add("${CR_LF}// ${NlsBundle.nls("redirect.times.req", redirectTimes)}${CR_LF}")

                    val locationUrl = ResUtils.resolveLocationUrl(url, response.headers())

                    handleHttp(locationUrl, LinkedMultiValueMap(), null, httpReqDescList)

                    return@executeOnPooledThread
                }

                if (hasError) {
                    try {
                        val httpInfo = HttpInfo(httpReqDescList, mutableListOf(), null, null, throwable)

                        dealResponse(httpInfo, parentPath)

                        detachProcess()
                    } catch (e: Exception) {
                        handleException(e)
                    }

                    return@executeOnPooledThread
                }

                val cookies = CookieUtils.parseAll(url, response.headers())

                var cookieSaveDesc = ""
                if (!paramMap.containsKey(ParamEnum.NO_COOKIE_JAR.param)) {
                    cookieSaveDesc = CookieUtils.saveCookiesToFile(cookies, project)
                }

                try {
                    val resHeaders = response.headers()
                    val resHeaderList = ResUtils.convertResponseHeaders(resHeaders)

                    val httpResInfo = ResUtils.convertResponseBody(response.body(), resHeaders)

                    val size = Formats.formatFileSize(response.body().size.toLong())
                    val comment = NlsBundle.nls("res.desc", response.statusCode(), costTimes!!, size)

                    val httpResDescList = mutableListOf<String>()

                    if (cookieSaveDesc.isNotEmpty()) {
                        httpResDescList.add("// $cookieSaveDesc${CR_LF}")
                    }

                    httpResDescList.add("// $comment${CR_LF}")

                    val evalJsRes = jsExecutor.evalJsAfterRequest(
                        url, reqBody, jsAfterReq, httpResInfo, response.statusCode(),
                        resHeaders.map(), cookies, httpFile.name, httpDocument
                    )

                    if (!evalJsRes.isNullOrEmpty()) {
                        httpResDescList.add("/*${CR_LF}${NlsBundle.nls("post.js.executed.result")}:${CR_LF}")
                        httpResDescList.add("$evalJsRes${CR_LF}")
                        httpResDescList.add("*/${CR_LF}")
                    }

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
                        httpResDescList.add(NlsBundle.nls("res.binary.data", size))
                    } else {
                        httpResDescList.add(bodyStr!!)
                    }

                    val httpInfo = HttpInfo(
                        httpReqDescList, httpResDescList, simpleTypeEnum, bodyBytes,
                        null, contentType, resHeaders
                    )

                    dealResponse(httpInfo, parentPath)

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