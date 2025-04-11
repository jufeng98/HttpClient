package org.javamaster.httpclient.dashboard

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpInfo
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.background.HttpBackground
import org.javamaster.httpclient.dubbo.DubboHandler
import org.javamaster.httpclient.dubbo.support.DubboJars
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvMap
import org.javamaster.httpclient.handler.RunFileHandler
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpReqInfo
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.HttpUtils
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

    private val preJsFiles = HttpUtils.getPreJsFiles(httpFile)

    private val jsListBeforeReq = HttpUtils.getAllPreJsScripts(httpFile, requestBlock)

    private val jsAfterReq = getJsScript(responseHandler)

    private val paramMap = HttpUtils.getDirectionCommentParamMap(requestBlock)

    private val httpDashboardForm = HttpDashboardForm(tabName, project)

    private val version = request.version?.version ?: Version.HTTP_1_1
    private var wsRequest: WsRequest? = null

    var hasError = false

    fun getComponent(): JPanel {
        return httpDashboardForm.mainPanel
    }

    override fun startNotify() {
        super.startNotify()

        HttpBackground
            .runInBackgroundReadActionAsync {
                preJsFiles.forEach {
                    try {
                        val content = VirtualFileUtils.readNewestContent(it.file)
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
        if (!DubboJars.jarsDownloaded()) {
            NotifyUtil.notifyCornerSuccess(
                project,
                "Execute dubbo request first time, downloading required dependencies. When finished, please try again."
            )

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

                val httpResDescList =
                    mutableListOf("// Time: ${consumeTimes}ms, size: ${byteArray.size / 1024.0}kb\r\n")

                val evalJsRes = jsExecutor.evalJsAfterRequest(
                    jsAfterReq,
                    Pair(SimpleTypeEnum.JSON, byteArray),
                    200,
                    mutableMapOf()
                )
                if (!evalJsRes.isNullOrEmpty()) {
                    httpResDescList.add("/*\r\npost js executed result:\r\n")
                    httpResDescList.add("$evalJsRes\r\n")
                    httpResDescList.add("*/\r\n")
                }

                httpResDescList.add("### $tabName\r\n")
                httpResDescList.add("DUBBO $url \r\n")
                httpResDescList.add("Content-Length: ${byteArray.size}\r\n")

                reqHeaderMap.forEach {
                    val name = it.key
                    it.value.forEach { value ->
                        httpResDescList.add("$name: $value\r\n")
                    }
                }
                httpResDescList.add("\r\n")

                httpResDescList.add(String(byteArray, StandardCharsets.UTF_8))

                val httpInfo = HttpInfo(httpReqDescList, httpResDescList, SimpleTypeEnum.JSON, byteArray, null)

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

                    val size = response.body().size / 1024.0
                    val consumeTimes = System.currentTimeMillis() - start

                    val resHeaderList = convertToResHeaderDescList(response)

                    val resPair = convertToResPair(response)

                    val httpResDescList =
                        mutableListOf("// Status: ${response.statusCode()}, time: ${consumeTimes}ms, size: $size KB\r\n")

                    val evalJsRes = jsExecutor.evalJsAfterRequest(
                        jsAfterReq,
                        resPair,
                        response.statusCode(),
                        response.headers().map()
                    )

                    if (!evalJsRes.isNullOrEmpty()) {
                        httpResDescList.add("/*\r\nPost js executed result:\r\n")
                        httpResDescList.add("$evalJsRes\r\n")
                        httpResDescList.add("*/\r\n")
                    }

                    val commentTabName = "### $tabName\r\n"
                    httpResDescList.add(commentTabName)

                    httpResDescList.add(methodType + " " + response.uri() + "\r\n")

                    httpResDescList.addAll(resHeaderList)

                    if (resPair.first != SimpleTypeEnum.IMAGE) {
                        httpResDescList.add(String(resPair.second, StandardCharsets.UTF_8))
                    }

                    val httpInfo = HttpInfo(httpReqDescList, httpResDescList, resPair.first, resPair.second, null)

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
        if (!saveResult.isNullOrEmpty()) {
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
                "The request $tabName has been interrupted!"
            } else {
                "$tabName request failed, exception: $myThrowable"
            }
            val msg = "<div style='font-size:13pt'>$error</div>"
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, MessageType.ERROR, msg)
        } else {
            val msg = "<div style='font-size:13pt'>$tabName request success!</div>"
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
            return "// Save to file failed: $e\r\n"
        }

        VirtualFileManager.getInstance().asyncRefresh(null)

        return "// Response has been saved to file: ${file.normalize().absolutePath}\r\n"
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
