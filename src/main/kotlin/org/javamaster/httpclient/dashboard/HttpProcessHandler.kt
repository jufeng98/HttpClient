package org.javamaster.httpclient.dashboard

import com.google.common.net.HttpHeaders
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.application
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.background.HttpBackground
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.consts.HttpConsts.Companion.FAILED
import org.javamaster.httpclient.consts.HttpConsts.Companion.SUCCESS
import org.javamaster.httpclient.consts.HttpConsts.Companion.WEB_BOUNDARY
import org.javamaster.httpclient.dashboard.support.JsTgz
import org.javamaster.httpclient.dubbo.DubboHandler
import org.javamaster.httpclient.dubbo.support.DubboJars
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvMap
import org.javamaster.httpclient.handler.RunFileHandler
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.js.support.jsObject.GlobalHeaders
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.mock.MockServer
import org.javamaster.httpclient.mock.support.MockServerHelper
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.model.HttpReqInfo
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.*
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.constructMultipartBodyCurl
import org.javamaster.httpclient.utils.HttpUtils.handleOrdinaryContentCurl
import org.javamaster.httpclient.ws.WsRequest
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.net.http.HttpClient.Version
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import javax.swing.JPanel

/**
 * @author yudong
 */
class HttpProcessHandler(val httpMethod: HttpMethod, private val selectedEnv: String?) : ProcessHandler() {
    val tabName = HttpUtils.getTabName(httpMethod)
    val project = httpMethod.project
    var httpStatus: Int? = null
    var costTimes: Long? = null
    var finishedTime = Long.MAX_VALUE
    var hasError = false

    private val httpFile = httpMethod.containingFile as HttpFile
    private val parentPath = httpFile.virtualFile.parent.path
    private val jsExecutor = JsExecutor(project, parentPath, tabName)
    private val variableResolver = VariableResolver(jsExecutor, httpFile, selectedEnv, project)
    private val loadingRemover = httpMethod.getUserData(HttpConsts.gutterIconLoadingKey)
    private val requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget::class.java)!!
    private val rawUrl = requestTarget.url
    private val request = PsiTreeUtil.getParentOfType(httpMethod, HttpRequest::class.java)!!
    private val requestBlock = PsiTreeUtil.getParentOfType(request, HttpRequestBlock::class.java)!!
    private val methodType = HttpRequestEnum.getInstance(httpMethod.text)
    private val responseHandler = PsiTreeUtil.getChildOfType(request, HttpResponseHandler::class.java)
    private val outPutFilePath = PsiTreeUtil.getChildOfType(request, HttpOutputFile::class.java)?.filePath?.text
    private val version = request.version?.version ?: Version.HTTP_1_1

    private val preJsFiles = HttpUtils.getPreJsFiles(httpFile, false, true)

    private val jsListBeforeReq = MyPsiUtils.getAllPreJsScripts(httpFile, requestBlock)

    private val jsAfterReq = MyPsiUtils.getJsScript(responseHandler)

    private val paramMap = MyPsiUtils.getReqDirectionCommentParamMap(requestBlock)

    private val httpDashboardForm by lazy {
        HttpDashboardForm(tabName, project)
    }

    private var wsRequest: WsRequest? = null
    private var mockServer: MockServer? = null
    private var redirectTimes = 0

    fun getComponent(): JPanel {
        return httpDashboardForm.mainPanel
    }

    override fun startNotify() {
        super.startNotify()

        application.executeOnPooledThread {
            try {
                if (preJsFiles.isEmpty()) {
                    startHandleRequest()

                    return@executeOnPooledThread
                }

                initPreJsFilesThenStartRequest()
            } catch (e: Exception) {
                runInEdt {
                    handleException(e)
                }
            }
        }
    }

    private fun initPreJsFilesThenStartRequest() {
        val preFilePair = preJsFiles.partition { it.urlFile != null }

        val npmFiles = preFilePair.first

        if (npmFiles.isEmpty()) {
            JsTgz.initJsLibrariesVirtualFile(preJsFiles)

            startHandleRequest()

            return
        }

        val npmFilesNotDownloaded = JsTgz.jsLibrariesNotDownloaded(npmFiles)

        if (npmFilesNotDownloaded.isNotEmpty()) {
            runInEdt {
                JsTgz.downloadAsyncInEdt(project, npmFilesNotDownloaded)

                httpDashboardForm.resetDashboardForm()
            }

            destroyProcess()

            return
        }

        JsTgz.initAndCacheNpmJsLibrariesFile(npmFiles, project)

        JsTgz.initJsLibrariesVirtualFile(preJsFiles)

        startHandleRequest()
    }

    private fun startHandleRequest() {
        ReqUtils.initPreJsFilesContent(preJsFiles, project, httpFile)

        var reqBody = HttpUtils.convertToReqBody(request, variableResolver, paramMap)

        val environment = ReadAction.compute<MutableMap<String, String>, Exception> { getEnvMap(project, false) }

        val fileCookies = CookieUtils.getValidFileCookieMap(project, false)

        val reqInfo = HttpReqInfo(reqBody, environment, preJsFiles, fileCookies)

        var url = variableResolver.resolve(rawUrl)

        runInEdt {
            httpDashboardForm.initLabelLoading(tabName, url)
        }

        url = ReqUtils.handleUrl(url)

        val httpHeaderFields = request.header?.headerFieldList

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(httpHeaderFields, variableResolver)

        jsExecutor.initJsRequestObj(
            url,
            rawUrl,
            request.body?.text,
            reqInfo,
            methodType,
            reqHeaderMap,
            selectedEnv,
            variableResolver.fileScopeVariableMap
        )

        val beforeJsResList = jsExecutor.evalJsBeforeRequest(reqInfo.preJsFiles, jsListBeforeReq)

        url = variableResolver.resolve(url)

        val httpReqDescList = mutableListOf<String>()
        httpReqDescList.addAll(beforeJsResList)

        reqHeaderMap = HttpUtils.resolveReqHeaderMapAgain(reqHeaderMap, variableResolver)

        if (paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)) {
            url = ReqUtils.encodeUrl(url)
        }

        reqBody = ReqUtils.resolveReqBodyAgain(reqInfo.reqBody, variableResolver)

        when (methodType) {
            HttpRequestEnum.WEBSOCKET -> {
                CookieUtils.addFileCookieToReqHeader(url, reqHeaderMap, reqInfo.fileCookies)

                reqHeaderMap.addAll(GlobalHeaders.dataHolder)

                handleWs(url, reqHeaderMap)
            }

            HttpRequestEnum.DUBBO -> handleDubbo(url, reqHeaderMap, reqBody, httpReqDescList)

            HttpRequestEnum.MOCK_SERVER -> handleMockServer()

            else -> {
                CookieUtils.addFileCookieToReqHeader(url, reqHeaderMap, reqInfo.fileCookies)

                reqHeaderMap.addAll(GlobalHeaders.dataHolder)

                handleHttp(url, reqHeaderMap, reqBody, httpReqDescList)
            }
        }
    }

    private fun handleMockServer() {
        loadingRemover?.run()

        val requestTarget = request.requestTarget ?: return

        val resConsumer = httpDashboardForm.initMockServerForm()

        val port = MockServerHelper.resolvePort(requestTarget.port)

        mockServer = MockServer(resConsumer, port)

        mockServer!!.startServer(request, variableResolver, paramMap)
    }

    fun prepareJsAndConvertToCurl(raw: Boolean, consumer: Consumer<String>) {
        val preFilePair = preJsFiles.partition { it.urlFile != null }

        val npmFiles = preFilePair.first

        if (npmFiles.isEmpty()) {
            convertToCurl(raw, consumer)

            return
        }

        val npmFilesNotDownloaded = JsTgz.jsLibrariesNotDownloaded(npmFiles)

        if (npmFilesNotDownloaded.isNotEmpty()) {
            JsTgz.downloadAsyncInEdt(project, npmFilesNotDownloaded) {
                application.executeOnPooledThread {
                    runReadAction {
                        JsTgz.initAndCacheNpmJsLibrariesFile(npmFiles, project)

                        convertToCurl(raw, consumer)
                    }
                }
            }

            httpDashboardForm.resetDashboardForm()

            return
        }

        application.executeOnPooledThread {
            runReadAction {
                JsTgz.initAndCacheNpmJsLibrariesFile(npmFiles, project)

                convertToCurl(raw, consumer)
            }
        }
    }

    private fun convertToCurl(raw: Boolean, consumer: Consumer<String>) {
        application.executeOnPooledThread {
            JsTgz.initJsLibrariesVirtualFile(preJsFiles)

            runInEdt {
                convertToCurlReal(raw, consumer)
            }
        }
    }

    private fun convertToCurlReal(raw: Boolean, consumer: Consumer<String>) {
        HttpBackground
            .runInBackgroundReadActionAsync {
                ReqUtils.initPreJsFilesContent(preJsFiles, project, httpFile)

                val reqBody = HttpUtils.convertToReqBody(request, variableResolver, paramMap)

                val environment = getEnvMap(project, false)

                val fileCookies = CookieUtils.getValidFileCookieMap(project, false)

                HttpReqInfo(reqBody, environment, preJsFiles, fileCookies)
            }
            .finishOnUiThread {
                convertToCurlReal(raw, consumer, it!!)
            }
            .exceptionallyOnUiThread {
                NotifyUtil.notifyError(project, it.toString())
            }
    }

    private fun convertToCurlReal(raw: Boolean, consumer: Consumer<String>, reqInfo: HttpReqInfo) {
        var url = variableResolver.resolve(rawUrl)

        val httpHeaderFields = request.header?.headerFieldList

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(httpHeaderFields, variableResolver)

        jsExecutor.initJsRequestObj(
            url,
            rawUrl,
            request.body?.text,
            reqInfo,
            methodType,
            reqHeaderMap,
            selectedEnv,
            variableResolver.fileScopeVariableMap
        )

        val resList = jsExecutor.evalJsBeforeRequest(reqInfo.preJsFiles, jsListBeforeReq)
        println("js执行结果:${resList}")

        url = variableResolver.resolve(url)

        reqHeaderMap = HttpUtils.resolveReqHeaderMapAgain(reqHeaderMap, variableResolver)

        if (paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)) {
            url = ReqUtils.encodeUrl(url)
        }

        val list = mutableListOf<String>()

        if (raw) {
            val tabName = HttpUtils.getTabName(request.method)
            list.add("### $tabName$CR_LF")
        }

        list.add(
            if (raw) {
                "${request.method.text} $url$CR_LF"
            } else {
                "curl -X ${request.method.text} --location \"$url\""
            }
        )

        reqHeaderMap.forEach {
            val name = it.key
            for (value in it.value) {
                list.add(
                    if (raw) {
                        "$name: $value$CR_LF"
                    } else {
                        "    -H \"$name: ${value}\""
                    }
                )
            }
        }

        if (raw) {
            list.add(CR_LF)
        }

        HttpBackground.runInBackgroundReadActionAsync {
            val header = request.header
            val body = request.body
            val requestMessagesGroup = body?.requestMessagesGroup
            val httpMultipartMessage = body?.multipartMessage

            if (requestMessagesGroup != null) {
                val content = handleOrdinaryContentCurl(requestMessagesGroup, variableResolver, header, raw)

                list.add(
                    if (raw) {
                        content
                    } else {
                        "    -d '${content}'"
                    }
                )
            } else if (httpMultipartMessage != null) {
                val boundary = request.contentTypeBoundary ?: WEB_BOUNDARY

                val contents = constructMultipartBodyCurl(httpMultipartMessage, variableResolver, boundary, raw)

                list.addAll(contents)
            }

            if (raw) {
                list.joinToString("")
            } else {
                list.joinToString(" \\${CR_LF}")
            }
        }.finishOnUiThread {
            consumer.accept(it!!)
        }.exceptionallyOnUiThread {
            NotifyUtil.notifyError(project, it.toString())
        }
    }

    private fun handleException(e: Exception) {
        httpDashboardForm.resetDashboardForm()
        destroyProcess()
        NotifyUtil.notifyError(project, "<div style='font-size:13pt'>${e}</div>")
    }

    private fun handleWs(url: String, reqHeaderMap: LinkedMultiValueMap<String, String?>) {
        loadingRemover?.run()

        wsRequest = WsRequest(url, reqHeaderMap, this, paramMap, httpDashboardForm)

        httpDashboardForm.initWsForm(wsRequest)

        wsRequest!!.connect()
    }

    private fun handleDubbo(
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
    ) {
        requestRunningSet.add(tabName)

        if (DubboJars.jarsNotDownloaded()) {
            DubboJars.downloadAsync(project)

            httpDashboardForm.resetDashboardForm()

            destroyProcess()
            return
        }

        val dubboRequest = ActionUtil.underModalProgress(project, "Processing dubbo...") {
            val module = ModuleUtil.findModuleForPsiElement(httpFile)

            val clsName = "org.javamaster.httpclient.dubbo.DubboRequest"
            val dubboRequestClazz = DubboJars.dubboClassLoader.loadClass(clsName)

            val constructor = dubboRequestClazz.declaredConstructors[0]
            constructor.isAccessible = true

            val dubboRequest: DubboHandler
            try {
                dubboRequest = constructor.newInstance(
                    tabName, url, reqHeaderMap, reqBody,
                    httpReqDescList, module, project, paramMap
                ) as DubboHandler
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }

            dubboRequest
        }

        val future = dubboRequest.sendAsync()

        future.whenCompleteAsync { triple, throwable ->

            runInEdt {
                application.runWriteAction {
                    if (throwable != null) {
                        val httpInfo = HttpInfo(httpReqDescList, mutableListOf(), null, null, throwable)

                        dealResponse(httpInfo, parentPath)

                        return@runWriteAction
                    }

                    httpStatus = 200
                    costTimes = triple.third

                    val bodyBytes = triple.first
                    val bodyStr = triple.second

                    val size = Formats.formatFileSize(bodyBytes.size.toLong())

                    val comment = nls("res.desc", 200, costTimes!!, size)

                    val httpResDescList = mutableListOf("// $comment$CR_LF")

                    val httpResInfo = HttpResInfo(
                        SimpleTypeEnum.JSON, bodyBytes, bodyStr,
                        ContentType.APPLICATION_JSON.mimeType
                    )

                    val evalJsRes = jsExecutor.evalJsAfterRequest(
                        url,
                        reqBody,
                        jsAfterReq,
                        httpResInfo,
                        200,
                        mutableMapOf(),
                        listOf()
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
                }
            }

            destroyProcess()
        }

        cancelFutureIfTerminated(future)
    }

    private fun handleHttp(
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
    ) {
        requestRunningSet.add(tabName)

        val start = System.currentTimeMillis()

        val methodTypeTmp = if (redirectTimes > 0) HttpRequestEnum.GET else methodType

        val future = methodTypeTmp.execute(url, version, reqHeaderMap, reqBody, httpReqDescList, tabName, paramMap)

        future.whenCompleteAsync { response, throwable ->
            costTimes = System.currentTimeMillis() - start
            httpStatus = response?.statusCode()

            if (ResUtils.shouldRedirect(httpStatus, paramMap)) {
                redirectTimes++
                if (redirectTimes > 6) {
                    return@whenCompleteAsync
                }

                runInEdt {
                    httpReqDescList.add("$CR_LF// ${nls("redirect.times.req", redirectTimes)}$CR_LF")

                    val locationUrl = ResUtils.resolveLocationUrl(url, response.headers())
                    handleHttp(locationUrl, LinkedMultiValueMap(), null, httpReqDescList)
                }

                return@whenCompleteAsync
            }

            if (throwable != null) {
                val httpInfo = HttpInfo(httpReqDescList, mutableListOf(), null, null, throwable)

                dealResponse(httpInfo, parentPath)

                return@whenCompleteAsync
            }

            try {
                val size = Formats.formatFileSize(response.body().size.toLong())

                val cookies = CookieUtils.parseAll(url, response.headers())

                var cookieSaveDesc = ""
                if (!paramMap.containsKey(ParamEnum.NO_COOKIE_JAR.param)) {
                    cookieSaveDesc = CookieUtils.saveCookiesToFile(cookies, project)
                }

                val resHeaders = response.headers()
                val resHeaderList = ResUtils.convertResponseHeaders(resHeaders)

                val httpResInfo = ResUtils.convertResponseBody(response.body(), resHeaders)

                val comment = nls("res.desc", response.statusCode(), costTimes!!, size)

                val httpResDescList = mutableListOf<String>()

                if (cookieSaveDesc.isNotEmpty()) {
                    httpResDescList.add("// $cookieSaveDesc$CR_LF")
                }

                httpResDescList.add("// $comment$CR_LF")

                val evalJsRes = jsExecutor.evalJsAfterRequest(
                    url,
                    reqBody,
                    jsAfterReq,
                    httpResInfo,
                    response.statusCode(),
                    resHeaders.map(),
                    cookies
                )

                if (!evalJsRes.isNullOrEmpty()) {
                    httpResDescList.add("/*$CR_LF${nls("post.js.executed.result")}:$CR_LF")
                    httpResDescList.add("$evalJsRes$CR_LF")
                    httpResDescList.add("*/$CR_LF")
                }

                val versionDesc = MyPsiUtils.getVersionDesc(response.version())

                val commentTabName = "### $tabName$CR_LF"
                httpResDescList.add(commentTabName)

                if (paramMap.containsKey(ParamEnum.VISUALIZE_TIMESTAMP.param)) {
                    httpResDescList.add("# @${ParamEnum.VISUALIZE_TIMESTAMP.param}$CR_LF")
                }

                httpResDescList.add(methodType.name + " " + response.uri() + " " + versionDesc + CR_LF)

                httpResDescList.addAll(resHeaderList)

                val simpleTypeEnum = httpResInfo.simpleTypeEnum
                val bodyBytes = httpResInfo.bodyBytes
                val bodyStr = httpResInfo.bodyStr
                val contentType = httpResInfo.contentType

                if (simpleTypeEnum.binary) {
                    httpResDescList.add(nls("res.binary.data", size))
                } else {
                    httpResDescList.add(bodyStr!!)
                }

                val httpInfo = HttpInfo(
                    httpReqDescList, httpResDescList, simpleTypeEnum, bodyBytes,
                    null, contentType, resHeaders
                )

                dealResponse(httpInfo, parentPath)
            } catch (e: Exception) {
                e.printStackTrace()

                NotifyUtil.notifyError(project, e.toString())
            }

            destroyProcess()
        }

        cancelFutureIfTerminated(future)
    }

    private fun dealResponse(httpInfo: HttpInfo, parentPath: String) {
        if (outPutFilePath != null && httpInfo.byteArray != null) {
            var path = variableResolver.resolve(outPutFilePath)
            path = HttpUtils.constructFilePath(path, parentPath)

            val saveResult = ResUtils.saveResToFile(path, httpInfo.byteArray)

            httpInfo.httpResDescList.add(0, saveResult)
        }

        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow(ToolWindowId.SERVICES)

        val content = toolWindow!!.contentManager.getContent(getComponent())
        if (content != null) {
            content.setDisposer(httpDashboardForm)
        } else {
            Disposer.register(Disposer.newDisposable(), httpDashboardForm)
        }

        httpDashboardForm.initHttpResContent(httpInfo, paramMap.containsKey(ParamEnum.NO_LOG.param))

        val myThrowable = httpDashboardForm.throwable
        hasError = myThrowable != null
        if (hasError) {
            myThrowable.printStackTrace()

            val error = if (myThrowable is CancellationException || myThrowable.cause is CancellationException) {
                nls("req.interrupted", tabName)
            } else {
                nls("req.failed", tabName, myThrowable)
            }
            val msg = "<div style='font-size:12pt'>$error</div>"
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, MessageType.ERROR, msg)
        } else {
            val msg = "<div style='font-size:12pt'>$tabName ${nls("request.success")}!</div>"
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, MessageType.INFO, msg)
        }

        finishedTime = System.currentTimeMillis()
    }

    private fun cancelFutureIfTerminated(future: CompletableFuture<*>) {
        CompletableFuture.runAsync {
            while (!isProcessTerminated && !RunFileHandler.isInterrupted()) {
                Thread.sleep(600)
            }

            if (loadingRemover != null) {
                runInEdt {
                    loadingRemover.run()
                }
            }

            future.cancel(true)
        }
    }

    override fun destroyProcessImpl() {
        if (loadingRemover != null) {
            runInEdt {
                loadingRemover.run()
            }
        }

        requestRunningSet.remove(tabName)

        wsRequest?.abortConnect()

        mockServer?.stopServer()

        val code = if (hasError) {
            FAILED
        } else {
            SUCCESS
        }

        httpMethod.putUserData(HttpConsts.requestFinishedKey, code)

        RunFileHandler.resetInterrupt()

        notifyProcessTerminated(code)
    }

    override fun detachProcessImpl() {
        destroyProcessImpl()

        notifyProcessDetached()
    }

    override fun detachIsDefault(): Boolean {
        return true
    }

    override fun getProcessInput(): OutputStream? {
        return null
    }

    companion object {
        private val requestRunningSet = mutableSetOf<String>()

        fun isRunning(tabName: String): Boolean {
            return requestRunningSet.contains(tabName)
        }
    }
}
