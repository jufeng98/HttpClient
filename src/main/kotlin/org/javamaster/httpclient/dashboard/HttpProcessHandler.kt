package org.javamaster.httpclient.dashboard

import com.google.common.net.HttpHeaders
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.application
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.background.HttpBackground
import org.javamaster.httpclient.dashboard.support.JsTgz
import org.javamaster.httpclient.dubbo.DubboHandler
import org.javamaster.httpclient.dubbo.support.DubboJars
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvMap
import org.javamaster.httpclient.handler.RunFileHandler
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.mock.MockServer
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.model.HttpReqInfo
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.FAILED
import org.javamaster.httpclient.utils.HttpUtils.SUCCESS
import org.javamaster.httpclient.utils.HttpUtils.WEB_BOUNDARY
import org.javamaster.httpclient.utils.HttpUtils.constructMultipartBodyCurl
import org.javamaster.httpclient.utils.HttpUtils.convertResponseHeaders
import org.javamaster.httpclient.utils.HttpUtils.convertResponseBody
import org.javamaster.httpclient.utils.HttpUtils.getJsScript
import org.javamaster.httpclient.utils.HttpUtils.gson
import org.javamaster.httpclient.utils.HttpUtils.handleOrdinaryContentCurl
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.VirtualFileUtils
import org.javamaster.httpclient.ws.WsRequest
import java.io.ByteArrayInputStream
import java.io.File
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.net.ServerSocket
import java.net.http.HttpClient.Version
import java.nio.file.Files
import java.nio.file.StandardCopyOption
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

    private val httpFile = httpMethod.containingFile as HttpFile
    private val parentPath = httpFile.virtualFile.parent.path
    private val jsExecutor = JsExecutor(project, httpFile, tabName)
    private val variableResolver = VariableResolver(jsExecutor, httpFile, selectedEnv, project)
    private val loadingRemover = httpMethod.getUserData(HttpUtils.gutterIconLoadingKey)
    private val requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget::class.java)!!
    private val request = PsiTreeUtil.getParentOfType(httpMethod, HttpRequest::class.java)!!
    private val requestBlock = PsiTreeUtil.getParentOfType(request, HttpRequestBlock::class.java)!!
    private val methodType = HttpRequestEnum.getInstance(httpMethod.text)
    private val responseHandler = PsiTreeUtil.getChildOfType(request, HttpResponseHandler::class.java)

    private val preJsFiles = HttpUtils.getPreJsFiles(httpFile, false)

    private val jsListBeforeReq = HttpUtils.getAllPreJsScripts(httpFile, requestBlock)

    private val jsAfterReq = getJsScript(responseHandler)

    private val paramMap = HttpUtils.getReqDirectionCommentParamMap(requestBlock)

    private val httpDashboardForm by lazy {
        HttpDashboardForm(tabName, project)
    }

    private val version = request.version?.version ?: Version.HTTP_1_1
    private var wsRequest: WsRequest? = null
    private var serverSocket: ServerSocket? = null

    var hasError = false

    fun getComponent(): JPanel {
        return httpDashboardForm.mainPanel
    }

    override fun startNotify() {
        super.startNotify()

        if (preJsFiles.isEmpty()) {
            startRequest()

            return
        }

        initNpmFilesThenStartRequest()
    }

    private fun initNpmFilesThenStartRequest() {
        val preFilePair = preJsFiles.partition { it.urlFile != null }

        val npmFiles = preFilePair.first

        if (npmFiles.isEmpty()) {
            initPreFilesThenStartRequest()

            return
        }

        val npmFilesNotDownloaded = JsTgz.jsLibrariesNotDownloaded(npmFiles)

        if (npmFilesNotDownloaded.isNotEmpty()) {
            JsTgz.downloadAsync(project, npmFilesNotDownloaded)

            destroyProcess()

            return
        }

        application.executeOnPooledThread {
            runReadAction {
                JsTgz.initAndCacheNpmJsLibrariesFile(npmFiles, project)

                initPreFilesThenStartRequest()
            }
        }
    }

    private fun initPreFilesThenStartRequest() {
        application.executeOnPooledThread {
            JsTgz.initJsLibrariesVirtualFile(preJsFiles)

            runInEdt {
                startRequest()
            }
        }
    }

    private fun initPreJsFilesContent() {
        preJsFiles.forEach {
            try {
                val content = VirtualFileUtils.readNewestContent(it.virtualFile)
                it.content = content
            } catch (e: Exception) {
                val document = PsiDocumentManager.getInstance(project).getDocument(httpFile)!!
                val rowNum = document.getLineNumber(it.directionComment.textOffset) + 1

                throw RuntimeException("$e(${httpFile.name}#${rowNum})", e)
            }
        }
    }

    private fun startRequest() {
        HttpBackground
            .runInBackgroundReadActionAsync {
                initPreJsFilesContent()

                val reqBody = HttpUtils.convertToReqBody(request, variableResolver, paramMap)

                val environment = gson.toJson(getEnvMap(project, false))

                HttpReqInfo(reqBody, environment, preJsFiles)
            }
            .finishOnUiThread {
                startHandleRequest(it!!)
            }
            .exceptionallyOnUiThread {
                handleException(it)
            }
    }

    private fun startHandleRequest(reqInfo: HttpReqInfo) {
        val httpHeaderFields = request.header?.headerFieldList

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(httpHeaderFields, variableResolver)

        jsExecutor.initJsRequestObj(
            reqInfo,
            methodType,
            reqHeaderMap,
            selectedEnv,
            variableResolver.fileScopeVariableMap
        )

        val beforeJsResList = jsExecutor.evalJsBeforeRequest(reqInfo.preJsFiles, jsListBeforeReq)

        val httpReqDescList = mutableListOf<String>()
        httpReqDescList.addAll(beforeJsResList)

        var url = variableResolver.resolve(requestTarget.url)

        if (paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)) {
            url = HttpUtils.encodeUrl(url)
        }

        reqHeaderMap = HttpUtils.resolveReqHeaderMapAgain(reqHeaderMap, variableResolver)

        reqHeaderMap.putAll(jsExecutor.getHeaderMap())

        val reqBody = reqInfo.reqBody

        when (methodType) {
            HttpRequestEnum.WEBSOCKET -> handleWs(url, reqHeaderMap)

            HttpRequestEnum.DUBBO -> handleDubbo(url, reqHeaderMap, reqBody, httpReqDescList)

            HttpRequestEnum.MOCK_SERVER -> handleMockServer()

            else -> handleHttp(url, reqHeaderMap, reqBody, httpReqDescList)
        }
    }

    private fun handleMockServer() {
        loadingRemover?.run()

        val mockServer = MockServer()

        httpDashboardForm.initMockServerForm(mockServer)

        serverSocket = mockServer.startServerAsync(request, variableResolver, paramMap)
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
            JsTgz.downloadAsync(project, npmFilesNotDownloaded) {
                application.executeOnPooledThread {
                    runReadAction {
                        JsTgz.initAndCacheNpmJsLibrariesFile(npmFiles, project)

                        convertToCurl(raw, consumer)
                    }
                }
            }

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
                initPreJsFilesContent()

                val reqBody = HttpUtils.convertToReqBody(request, variableResolver, paramMap)

                val environment = gson.toJson(getEnvMap(project, false))

                HttpReqInfo(reqBody, environment, preJsFiles)
            }
            .finishOnUiThread {
                convertToCurlReal(raw, consumer, it!!)
            }
            .exceptionallyOnUiThread {
                NotifyUtil.notifyError(project, it.toString())
            }
    }

    private fun convertToCurlReal(raw: Boolean, consumer: Consumer<String>, reqInfo: HttpReqInfo) {
        val httpHeaderFields = request.header?.headerFieldList

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(httpHeaderFields, variableResolver)

        jsExecutor.initJsRequestObj(
            reqInfo,
            methodType,
            reqHeaderMap,
            selectedEnv,
            variableResolver.fileScopeVariableMap
        )

        val resList = jsExecutor.evalJsBeforeRequest(reqInfo.preJsFiles, jsListBeforeReq)
        println("js执行结果:${resList}")

        var url = variableResolver.resolve(requestTarget.url)

        if (paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)) {
            url = HttpUtils.encodeUrl(url)
        }

        reqHeaderMap = HttpUtils.resolveReqHeaderMapAgain(reqHeaderMap, variableResolver)

        reqHeaderMap.putAll(jsExecutor.getHeaderMap())

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
        destroyProcess()
        NotifyUtil.notifyError(project, "<div style='font-size:13pt'>${e}</div>")
    }

    private fun handleWs(url: String, reqHeaderMap: LinkedMultiValueMap<String, String>) {
        loadingRemover?.run()

        wsRequest = WsRequest(url, reqHeaderMap, this, paramMap, httpDashboardForm)

        httpDashboardForm.initWsForm(wsRequest)

        wsRequest!!.connect()
    }

    private fun handleDubbo(
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
    ) {
        if (DubboJars.jarsNotDownloaded()) {
            DubboJars.downloadAsync(project)

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
                        jsAfterReq,
                        httpResInfo,
                        200,
                        mutableMapOf()
                    )

                    if (!evalJsRes.isNullOrEmpty()) {
                        httpResDescList.add("/*$CR_LF${nls("post.js.executed.result")}:$CR_LF")
                        httpResDescList.add("$evalJsRes$CR_LF")
                        httpResDescList.add("*/$CR_LF")
                    }

                    httpResDescList.add("### $tabName$CR_LF")
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
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
    ) {
        val start = System.currentTimeMillis()

        val future = methodType.execute(url, version, reqHeaderMap, reqBody, httpReqDescList, tabName, paramMap)

        future.whenCompleteAsync { response, throwable ->

            costTimes = System.currentTimeMillis() - start

            runInEdt {
                application.runWriteAction {
                    try {
                        httpStatus = response?.statusCode()

                        if (throwable != null) {
                            val httpInfo = HttpInfo(httpReqDescList, mutableListOf(), null, null, throwable)

                            dealResponse(httpInfo, parentPath)

                            return@runWriteAction
                        }

                        val size = Formats.formatFileSize(response.body().size.toLong())

                        val resHeaderList = convertResponseHeaders(response.headers())

                        val httpResInfo = convertResponseBody(response.body(), response.headers())

                        val comment = nls("res.desc", response.statusCode(), costTimes!!, size)

                        val httpResDescList = mutableListOf("// $comment$CR_LF")

                        val evalJsRes = jsExecutor.evalJsAfterRequest(
                            jsAfterReq,
                            httpResInfo,
                            response.statusCode(),
                            response.headers().map()
                        )

                        if (!evalJsRes.isNullOrEmpty()) {
                            httpResDescList.add("/*$CR_LF${nls("post.js.executed.result")}:$CR_LF")
                            httpResDescList.add("$evalJsRes$CR_LF")
                            httpResDescList.add("*/$CR_LF")
                        }

                        val versionDesc = HttpUtils.getVersionDesc(response.version())

                        val commentTabName = "### $tabName$CR_LF"
                        httpResDescList.add(commentTabName)

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
                            null, contentType
                        )

                        dealResponse(httpInfo, parentPath)
                    } catch (e: Exception) {
                        e.printStackTrace()

                        NotifyUtil.notifyError(project, e.toString())
                    }
                }
            }

            destroyProcess()
        }

        cancelFutureIfTerminated(future)
    }

    private fun dealResponse(httpInfo: HttpInfo, parentPath: String) {
        val requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget::class.java)!!

        val httpRequest = PsiTreeUtil.getParentOfType(requestTarget, HttpRequest::class.java)!!

        var outPutFilePath: String? = null
        val httpOutputFile = PsiTreeUtil.getChildOfType(httpRequest, HttpOutputFile::class.java)
        if (httpOutputFile != null) {
            outPutFilePath = httpOutputFile.filePath!!.text
        }

        val saveResult = saveResToFile(outPutFilePath, parentPath, httpInfo.byteArray)
        if (saveResult != null) {
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

    private fun saveResToFile(outPutFilePath: String?, parentPath: String, byteArray: ByteArray?): String? {
        if (outPutFilePath == null) {
            return null
        }

        if (byteArray == null) {
            return null
        }

        var path = variableResolver.resolve(outPutFilePath)

        path = HttpUtils.constructFilePath(path, parentPath)

        val file = File(path)

        if (!file.parentFile.exists()) {
            Files.createDirectories(file.toPath())
        }

        try {
            ByteArrayInputStream(byteArray).use {
                Files.copy(it, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "// ${nls("save.failed")}: $e$CR_LF"
        }

        VirtualFileManager.getInstance().asyncRefresh(null)

        return "// ${nls("save.to.file")}: ${file.normalize().absolutePath}$CR_LF"
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

        wsRequest?.abortConnect()

        serverSocket?.close()

        val code = if (hasError) {
            FAILED
        } else {
            SUCCESS
        }

        httpMethod.putUserData(HttpUtils.requestFinishedKey, code)

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

}
