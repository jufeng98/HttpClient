package org.javamaster.httpclient.dashboard

import com.google.common.net.HttpHeaders
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.runWriteActionAndWait
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
import org.javamaster.httpclient.HttpInfo
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.background.HttpBackground
import org.javamaster.httpclient.dashboard.support.JsTgz
import org.javamaster.httpclient.dubbo.DubboHandler
import org.javamaster.httpclient.dubbo.support.DubboJars
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvMap
import org.javamaster.httpclient.handler.RunFileHandler
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpReqInfo
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.FAILED
import org.javamaster.httpclient.utils.HttpUtils.SUCCESS
import org.javamaster.httpclient.utils.HttpUtils.convertToResHeaderDescList
import org.javamaster.httpclient.utils.HttpUtils.convertToResPair
import org.javamaster.httpclient.utils.HttpUtils.getJsScript
import org.javamaster.httpclient.utils.HttpUtils.gson
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.VirtualFileUtils
import org.javamaster.httpclient.ws.WsRequest
import java.io.ByteArrayInputStream
import java.io.File
import java.io.OutputStream
import java.net.http.HttpClient.Version
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import javax.swing.JPanel

/**
 * @author yudong
 */
class HttpProcessHandler(private val httpMethod: HttpMethod, selectedEnv: String?) : ProcessHandler() {
    val tabName = HttpUtils.getTabName(httpMethod)
    val project = httpMethod.project

    private val httpFile = httpMethod.containingFile as HttpFile
    private val parentPath = httpFile.virtualFile.parent.path
    private val jsExecutor = JsExecutor(project, httpFile, tabName)
    private val variableResolver = VariableResolver(jsExecutor, httpFile, selectedEnv)
    private val loadingRemover = httpMethod.getUserData(HttpUtils.gutterIconLoadingKey)
    private val requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget::class.java)!!
    private val request = PsiTreeUtil.getParentOfType(httpMethod, HttpRequest::class.java)!!
    private val requestBlock = PsiTreeUtil.getParentOfType(request, HttpRequestBlock::class.java)!!
    private val methodType = httpMethod.text
    private val responseHandler = PsiTreeUtil.getChildOfType(request, HttpResponseHandler::class.java)

    private val preJsFiles = HttpUtils.getPreJsFiles(httpFile, false)

    private val jsListBeforeReq = HttpUtils.getAllPreJsScripts(httpFile, requestBlock)

    private val jsAfterReq = getJsScript(responseHandler)

    private val paramMap = HttpUtils.getReqDirectionCommentParamMap(requestBlock)

    private val httpDashboardForm = HttpDashboardForm(tabName, project)

    private val version = request.version?.version ?: Version.HTTP_1_1
    private var wsRequest: WsRequest? = null

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
                JsTgz.initJsLibrariesFile(npmFiles, project)

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

    private fun startRequest() {
        HttpBackground
            .runInBackgroundReadActionAsync {
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

                val reqBody = HttpUtils.convertToReqBody(request, variableResolver)

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

        jsExecutor.initJsRequestObj(reqInfo, methodType, reqHeaderMap)

        val beforeJsResList = jsExecutor.evalJsBeforeRequest(reqInfo.preJsFiles, jsListBeforeReq)

        val httpReqDescList = mutableListOf<String>()
        httpReqDescList.addAll(beforeJsResList)

        val url = variableResolver.resolve(requestTarget.url)

        reqHeaderMap = HttpUtils.resolveReqHeaderMapAgain(reqHeaderMap, variableResolver)

        if (methodType == HttpRequestEnum.WEBSOCKET.name) {
            handleWs(url, reqHeaderMap)
            return
        }

        val reqBody = reqInfo.reqBody

        if (methodType == HttpRequestEnum.DUBBO.name) {
            handleDubbo(url, reqHeaderMap, reqBody, httpReqDescList)
            return
        }

        handleHttp(url, reqHeaderMap, reqBody, httpReqDescList)
    }

    private fun handleException(e: Exception) {
        destroyProcess()
        NotifyUtil.notifyError(project, "<div style='font-size:13pt'>${e}</div>")
    }

    private fun handleWs(url: String, reqHeaderMap: LinkedMultiValueMap<String, String>) {
        loadingRemover?.run()

        wsRequest = WsRequest(url, reqHeaderMap, this, paramMap)

        httpDashboardForm.initWsResData(wsRequest)

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

            val dubboRequest = constructor.newInstance(
                tabName, url, reqHeaderMap, reqBody,
                httpReqDescList, module, project, paramMap
            ) as DubboHandler

            return@underModalProgress dubboRequest
        }

        val future = dubboRequest.sendAsync()

        future.whenCompleteAsync { pair, throwable ->
            runWriteActionAndWait {
                if (throwable != null) {
                    val info = HttpInfo(httpReqDescList, mutableListOf(), null, null, throwable)

                    dealResponse(info, parentPath)

                    return@runWriteActionAndWait
                }

                val byteArray = pair.first
                val consumeTimes = pair.second

                val size = Formats.formatFileSize(byteArray.size.toLong())

                val comment = nls("res.desc", 200, consumeTimes, size)

                val httpResDescList = mutableListOf("// $comment$CR_LF")

                val evalJsRes = jsExecutor.evalJsAfterRequest(
                    jsAfterReq,
                    Triple(SimpleTypeEnum.JSON, byteArray, ContentType.APPLICATION_JSON.mimeType),
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
                httpResDescList.add("${HttpHeaders.CONTENT_LENGTH}: ${byteArray.size}$CR_LF")

                reqHeaderMap.forEach {
                    val name = it.key
                    it.value.forEach { value ->
                        httpResDescList.add("$name: $value$CR_LF")
                    }
                }
                httpResDescList.add(CR_LF)

                httpResDescList.add(String(byteArray, StandardCharsets.UTF_8))

                val httpInfo = HttpInfo(
                    httpReqDescList,
                    httpResDescList,
                    SimpleTypeEnum.JSON,
                    byteArray,
                    null,
                    ContentType.APPLICATION_JSON.mimeType
                )

                dealResponse(httpInfo, parentPath)
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

        val requestEnum = HttpRequestEnum.getInstance(methodType)
        val future = requestEnum.execute(url, version, reqHeaderMap, reqBody, httpReqDescList, tabName, paramMap)

        future.whenCompleteAsync { response, throwable ->
            runWriteActionAndWait {
                try {
                    if (throwable != null) {
                        val httpInfo = HttpInfo(httpReqDescList, mutableListOf(), null, null, throwable)
                        dealResponse(httpInfo, parentPath)
                        return@runWriteActionAndWait
                    }

                    val size = Formats.formatFileSize(response.body().size.toLong())

                    val consumeTimes = System.currentTimeMillis() - start

                    val resHeaderList = convertToResHeaderDescList(response)

                    val resTriple = convertToResPair(response)

                    val comment = nls("res.desc", response.statusCode(), consumeTimes, size)

                    val httpResDescList = mutableListOf("// $comment$CR_LF")

                    val evalJsRes = jsExecutor.evalJsAfterRequest(
                        jsAfterReq,
                        resTriple,
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

                    httpResDescList.add(methodType + " " + response.uri() + " " + versionDesc + CR_LF)

                    httpResDescList.addAll(resHeaderList)

                    if (resTriple.first.binary) {
                        httpResDescList.add(nls("res.binary.data", size))
                    } else {
                        httpResDescList.add(String(resTriple.second, StandardCharsets.UTF_8))
                    }

                    val httpInfo = HttpInfo(
                        httpReqDescList, httpResDescList, resTriple.first, resTriple.second,
                        null, resTriple.third
                    )

                    dealResponse(httpInfo, parentPath)
                } catch (e: Exception) {
                    e.printStackTrace()

                    NotifyUtil.notifyError(project, e.toString())
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

        httpDashboardForm.initHttpResContent(httpInfo)

        val myThrowable = httpDashboardForm.throwable
        hasError = myThrowable != null
        if (hasError) {
            myThrowable.printStackTrace()

            val error = if (myThrowable is CancellationException || myThrowable.cause is CancellationException) {
                nls("req.interrupted", tabName)
            } else {
                nls("req.failed", tabName, myThrowable)
            }
            val msg = "<div style='font-size:13pt'>$error</div>"
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, MessageType.ERROR, msg)
        } else {
            val msg = "<div style='font-size:13pt'>$tabName ${nls("request.success")}!</div>"
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, MessageType.INFO, msg)
        }
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
                runWriteActionAndWait {
                    loadingRemover.run()
                }
            }

            future.cancel(true)
        }
    }

    override fun destroyProcessImpl() {
        if (loadingRemover != null) {
            runWriteActionAndWait {
                loadingRemover.run()
            }
        }

        wsRequest?.abortConnect()

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
