package org.javamaster.httpclient.processHandler

import com.intellij.json.psi.JsonObject
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.consts.HttpConsts.Companion.RES_SIZE_LIMIT
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.exception.JsScriptException
import org.javamaster.httpclient.js.support.JsExecuteResult
import org.javamaster.httpclient.js.support.jsObject.GlobalHeaders
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.*
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext


/**
 * @author yudong
 */
class HttpProcessHandler(httpMethod: HttpMethod, selectedEnv: String?) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var redirectTimes = 0
    private var sslObj: JsonObject? = null
    private var verifyCert: Boolean = true
    private var clientCertificatePath: String? = null
    private var hasCertificatePassphrase: Boolean = false

    override fun startProcess() {
        requestRunningSet.add(tabName)

        var url = resolveAndHandleUrl()

        runInEdt { httpDashboardForm.initLabelLoading(tabName, url) }

        val reqInfo = createHttpReqInfo()

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(request.header?.headerFieldList, variableResolver)

        val preJsResList = executePreJs(url, reqInfo, reqHeaderMap)

        val jsBeforeExecuteResult = JsExecuteResult(preJsResList, jsScriptException)

        // 由于 前置 js 处理器有可能新增或修改了变量,所以 url、header、body 都需要重新解析一遍
        url = HttpUtils.resolveUrlAgain(url, variableResolver)

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
        val httpResDescList = mutableListOf<String>()
        val jsResList = mutableListOf<String>()

        val envFileService = EnvFileService.getService(project)

        HttpUtils.runReadAction {
            sslObj = envFileService.getEnvObj(HttpConsts.SSL_CONFIGURATION, selectedEnv, parentPath)
            verifyCert = JsonUtil.getBoolValue(sslObj, "verifyHostCertificate", true)
            clientCertificatePath = JsonUtil.getStrValue(sslObj, "clientCertificate")
            hasCertificatePassphrase = JsonUtil.getBoolValue(sslObj, "hasCertificatePassphrase", false)
        }

        if (verifyCert) {
            if (clientCertificatePath == null) {
                handleHttp(
                    url, reqHeaderMap, reqBody,
                    httpReqDescList, httpResDescList, jsResList,
                    jsBeforeExecuteResult, null
                )
            } else {
                val certPath = HttpUtils.constructFilePath(
                    clientCertificatePath!!, computeReadAction { sslObj!!.containingFile.virtualFile.parent.path }
                )

                var pwd: String? = null
                if (hasCertificatePassphrase) {
                    pwd = SensitiveDataUtil.get(HttpConsts.CERT_PWD)
                    if (pwd == null) {
                        application.invokeAndWait {
                            pwd = Messages.showPasswordDialog(
                                NlsBundle.nls("cert.pwd.enter.hint"), "Http Request Secured Value"
                            )
                        }

                        if (pwd == null) {
                            throw IllegalArgumentException("请输入证书密码!")
                        }

                        // 先检查密码是否正确
                        SslUtil.clientP12Cert(certPath, pwd)

                        SensitiveDataUtil.save(HttpConsts.CERT_PWD, pwd!!)
                    }
                }

                val sSLContext = SslUtil.clientP12Cert(certPath, pwd)

                handleHttp(
                    url, reqHeaderMap, reqBody,
                    httpReqDescList, httpResDescList, jsResList,
                    jsBeforeExecuteResult, sSLContext
                )
            }
        } else {
            handleHttp(
                url, reqHeaderMap, reqBody,
                httpReqDescList, httpResDescList, jsResList,
                jsBeforeExecuteResult, SslUtil.trustAllCert()
            )
        }
    }

    private fun handleHttp(
        url: String,
        reqHeaderMap: MultiValueMap<String, String?>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
        httpResDescList: MutableList<String>,
        jsResList: MutableList<String>,
        jsBeforeExecuteResult: JsExecuteResult?,
        sslContext: SSLContext?,
    ) {
        val targetMethodType = if (redirectTimes > 0) HttpRequestEnum.GET else methodType

        if (targetMethodType == HttpRequestEnum.CUSTOM) {
            paramMap["METHOD"] = httpMethod.text
        }

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

        val future = targetMethodType.execute(
            paramMap, req, sslContext, { runInEdt { httpDashboardForm.initProgress(it) } }
        ) { it1, it2 ->
            runInEdt { httpDashboardForm.updateProgress(it1, it2) }
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

                    handleHttp(
                        locationUrl, reqHeaderMap, null,
                        httpReqDescList, httpResDescList, jsResList,
                        null, sslContext
                    )

                    return@executeOnPooledThread
                }

                if (repeatExecutor != null) {
                    repeatExecutorLast = repeatExecutor

                    val arrayIndex = repeatExecutor!!.arrayIndex
                    if (arrayIndex > 0) {
                        httpResDescList.add("${CR_LF}// 序号${arrayIndex}${CR_LF}")
                    }

                    repeatExecutor!!.arrayIndex += 1

                    if (repeatExecutor!!.arrayIndex < repeatExecutor!!.paramNativeArray.size) {
                        httpReqDescList.add("${CR_LF}// 序号${arrayIndex + 1}${CR_LF}")

                        var url1 = resolveAndHandleUrl()

                        var reqHeaderMap1 = HttpUtils.convertToReqHeaderMap(
                            request.header?.headerFieldList, variableResolver
                        )

                        val reqInfo1 = createHttpReqInfo()

                        ResUtils.handleResponse(
                            url1, response, httpReqDescList, httpResDescList, this, targetMethodType,
                            reqInfo1.reqBody, jsResList
                        )

                        handleHttp(
                            url1, reqHeaderMap1, reqInfo1.reqBody,
                            httpReqDescList, httpResDescList, jsResList,
                            null, sslContext
                        )

                        return@executeOnPooledThread
                    } else {
                        repeatExecutor = null
                    }
                }

                try {
                    if (hasReqError) {
                        val httpInfo = HttpInfo(httpReqDescList, mutableListOf(), null, null, throwable)
                        httpInfo.reqContentLength = reqContentLength
                        httpInfo.jsBeforeExecuteResult = jsBeforeExecuteResult

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

                    jsResList.addAll(resList)

                    val jsAfterExecuteResult = JsExecuteResult(jsResList, jsScriptException)

                    val versionDesc = MyPsiUtils.Companion.getVersionDesc(response.version())

                    val commentTabName = "### $tabName${CR_LF}"
                    httpResDescList.add(commentTabName)

                    if (paramMap.containsKey(ParamEnum.VISUALIZE_TIMESTAMP.param)) {
                        httpResDescList.add("# @${ParamEnum.VISUALIZE_TIMESTAMP.param}${CR_LF}")
                    }

                    val methodName = if (targetMethodType == HttpRequestEnum.CUSTOM) {
                        httpMethod.text
                    } else {
                        targetMethodType.name
                    }

                    httpResDescList.add(methodName + " " + response.uri() + " " + versionDesc + CR_LF)

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