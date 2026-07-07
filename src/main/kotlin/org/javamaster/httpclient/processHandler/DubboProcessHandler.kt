package org.javamaster.httpclient.processHandler

import com.google.common.net.HttpHeaders
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.module.ModuleUtil
import com.intellij.util.application
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.dubbo.support.DubboJars
import org.javamaster.httpclient.dubbo.support.DubboRequest
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.exception.JsScriptException
import org.javamaster.httpclient.js.support.JsExecuteResult
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.ExecutorUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import org.javamaster.httpclient.utils.ReqUtils
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
class DubboProcessHandler(httpMethod: HttpMethod, selectedEnv: String?) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    override fun startProcess() {
        requestRunningSet.add(tabName)

        var url = resolveAndHandleUrl()

        runInEdt {
            httpDashboardForm.initLabelLoading(tabName, url)
            httpDashboardForm.initProgress(-1)
        }

        val reqInfo = createHttpReqInfo()

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(request.header?.headerFieldList, variableResolver)

        val preJsResList = executePreJs(url, reqInfo, reqHeaderMap)

        val jsBeforeExecuteResult = JsExecuteResult(preJsResList, jsScriptException)

        // 由于 前置 js 处理器有可能新增或修改了变量,所以 url、header、body 都需要重新解析一遍
        url = variableResolver.resolve(url)

        if (paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)) {
            url = ReqUtils.encodeUrl(url)
        }

        reqHeaderMap = HttpUtils.resolveReqHeaderMapAgain(reqHeaderMap, variableResolver)

        val reqBody = ReqUtils.resolveReqBodyAgain(reqInfo.reqBody, variableResolver, paramMap)

        handleDubbo(url, reqHeaderMap, reqBody, jsBeforeExecuteResult)
    }

    private fun handleDubbo(
        url: String,
        reqHeaderMap: MultiValueMap<String, String?>,
        reqBody: Any?,
        jsBeforeExecuteResult: JsExecuteResult?,
    ) {
        val httpReqDescList = mutableListOf<String>()

        val module = computeReadAction { ModuleUtil.findModuleForPsiElement(httpFile) }

        val clsName = "org.javamaster.httpclient.dubbo.DubboRequestImpl"
        val dubboRequestClazz = DubboJars.dubboClassLoader.loadClass(clsName)

        val constructor = dubboRequestClazz.declaredConstructors[0]
        constructor.isAccessible = true

        val dubboRequest: DubboRequest
        try {
            dubboRequest = computeReadAction {
                constructor.newInstance(
                    tabName, url, reqHeaderMap, reqBody, httpReqDescList, module, project, paramMap
                ) as DubboRequest
            }
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }

        if (jsScriptException != null) {
            dealPreJsErrorResponse(httpReqDescList, jsBeforeExecuteResult)

            return
        }

        var elapseTime = 0
        elapseTimeFuture = ExecutorUtils.scheduledExecutor.scheduleAtFixedRate(Runnable {
            runInEdt { httpDashboardForm.updateLabelLoading(++elapseTime) }
        }, 1, 1, TimeUnit.SECONDS)

        val future = dubboRequest.sendAsync()

        this.future = future

        future.whenCompleteAsync { triple, throwable ->
            costTimes = triple?.third
            hasReqError = throwable != null

            application.executeOnPooledThread {
                try {
                    if (hasReqError) {
                        val httpInfo = HttpInfo(httpReqDescList, mutableListOf(), null, null, throwable)
                        httpInfo.reqContentLength = triple?.first?.size?.toLong()

                        dealResponse(httpInfo)

                        detachProcess()

                        return@executeOnPooledThread
                    }

                    httpStatus = 200

                    val bodyBytes = triple!!.first
                    val bodyStr = triple.second
                    val reqContentLength = bodyBytes.size.toLong()

                    val httpResInfo = HttpResInfo(
                        SimpleTypeEnum.JSON, bodyBytes, bodyStr,
                        ContentType.APPLICATION_JSON.mimeType
                    )

                    var resList: List<String>
                    try {
                        resList = jsExecutor.evalJsAfterRequest(
                            url, reqBody, jsAfterReq, httpResInfo, httpStatus!!,
                            mutableMapOf(), listOf(), httpFile.name, httpDocument
                        )
                    } catch (e: JsScriptException) {
                        jsScriptException = e

                        resList = e.list
                    }

                    val jsAfterExecuteResult = JsExecuteResult(resList, jsScriptException)

                    val httpResDescList = mutableListOf<String>()

                    httpResDescList.add("### $tabName$CR_LF")

                    if (paramMap.containsKey(ParamEnum.VISUALIZE_TIMESTAMP.param)) {
                        httpResDescList.add("# @${ParamEnum.VISUALIZE_TIMESTAMP.param}$CR_LF")
                    }

                    httpResDescList.add("DUBBO $url $CR_LF")
                    httpResDescList.add("${HttpHeaders.CONTENT_LENGTH}: ${bodyBytes.size}$CR_LF")

                    reqHeaderMap.forEach {
                        val name = it.key
                        it.value.forEach { value ->
                            httpResDescList.add("$name: $value$CR_LF")
                        }
                    }
                    httpResDescList.add(CR_LF)

                    httpResDescList.add(bodyStr)

                    val httpInfo = HttpInfo(
                        httpReqDescList, httpResDescList, SimpleTypeEnum.JSON, bodyBytes,
                        null, ContentType.APPLICATION_JSON.mimeType, null, resolveOutputFilePath(), null, httpStatus!!,
                        costTimes, bodyBytes.size, jsBeforeExecuteResult, jsAfterExecuteResult, reqContentLength
                    )

                    dealResponse(httpInfo)

                    detachProcess()
                } catch (e: Exception) {
                    handleException(e)
                }
            }
        }
    }

    override fun downloadOtherFiles(): Boolean {
        if (DubboJars.jarsDownloaded()) {
            return true
        }

        DubboJars.downloadAsync(project)

        runInEdt { httpDashboardForm.resetDashboardForm() }

        return false
    }

    override fun destroyProcessImpl() {
        runInEdt { loadingRemover?.run() }

        requestRunningSet.remove(tabName)

        super.destroyProcessImpl()
    }
}