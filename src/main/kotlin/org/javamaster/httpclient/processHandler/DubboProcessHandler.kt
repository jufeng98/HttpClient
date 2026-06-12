package org.javamaster.httpclient.processHandler

import com.google.common.net.HttpHeaders
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.text.Formats
import com.intellij.util.application
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.dubbo.DubboHandler
import org.javamaster.httpclient.dubbo.support.DubboJars
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import org.javamaster.httpclient.utils.ReqUtils
import java.lang.reflect.InvocationTargetException

/**
 * @author yudong
 */
class DubboProcessHandler(httpMethod: HttpMethod, selectedEnv: String?) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

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
            url = ReqUtils.encodeUrl(url)
        }

        val reqBody = ReqUtils.resolveReqBodyAgain(reqInfo.reqBody, variableResolver)

        handleDubbo(url, reqHeaderMap, reqBody, httpReqDescList)
    }

    private fun handleDubbo(
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
    ) {
        val module = computeReadAction { ModuleUtil.findModuleForPsiElement(httpFile) }

        val clsName = "org.javamaster.httpclient.dubbo.DubboRequest"
        val dubboRequestClazz = DubboJars.dubboClassLoader.loadClass(clsName)

        val constructor = dubboRequestClazz.declaredConstructors[0]
        constructor.isAccessible = true

        val dubboRequest: DubboHandler
        try {
            dubboRequest = computeReadAction {
                constructor.newInstance(
                    tabName, url, reqHeaderMap, reqBody, httpReqDescList, module, project, paramMap
                ) as DubboHandler
            }
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }

        val future = dubboRequest.sendAsync()

        this.future = future

        future.whenCompleteAsync { triple, throwable ->
            costTimes = triple?.third
            hasError = throwable != null

            application.executeOnPooledThread {
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

                try {
                    httpStatus = 200

                    val bodyBytes = triple!!.first
                    val bodyStr = triple.second

                    val size = Formats.formatFileSize(bodyBytes.size.toLong())

                    val comment = nls("res.desc", httpStatus!!, costTimes!!, size)

                    val httpResDescList = mutableListOf("// $comment$CR_LF")

                    val httpResInfo = HttpResInfo(
                        SimpleTypeEnum.JSON, bodyBytes, bodyStr,
                        ContentType.APPLICATION_JSON.mimeType
                    )

                    val evalJsRes = jsExecutor.evalJsAfterRequest(
                        url, reqBody, jsAfterReq, httpResInfo, httpStatus!!,
                        mutableMapOf(), listOf(), httpFile.name, httpDocument
                    )

                    if (!evalJsRes.isNullOrEmpty()) {
                        httpResDescList.add("/*$CR_LF${nls("post.js.executed.result")}:$CR_LF")
                        httpResDescList.add("$evalJsRes$CR_LF")
                        httpResDescList.add("*/$CR_LF")
                    }

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
                        httpReqDescList,
                        httpResDescList,
                        SimpleTypeEnum.JSON,
                        bodyBytes,
                        null,
                        ContentType.APPLICATION_JSON.mimeType
                    )

                    dealResponse(httpInfo, parentPath)

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