package org.javamaster.httpclient.dashboard

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer.newDisposable
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiTreeUtil
import org.apache.commons.lang3.exception.ExceptionUtils
import org.javamaster.httpclient.HttpInfo
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.dubbo.DubboRequest
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.js.JsScriptExecutor
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.convertToResHeaderDescList
import org.javamaster.httpclient.utils.HttpUtils.convertToResPair
import org.javamaster.httpclient.utils.HttpUtils.getJsScript
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.ws.WsRequest
import java.io.ByteArrayInputStream
import java.io.File
import java.io.OutputStream
import java.net.http.HttpClient.Version
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import javax.swing.JPanel

class HttpProcessHandler(
    private val httpMethod: HttpMethod,
    private val selectedEnv: String?,
) : ProcessHandler() {
    val tabName = HttpUtils.getTabName(httpMethod)
    val project = httpMethod.project

    private val httpFile = httpMethod.containingFile
    private val parentPath = httpFile.virtualFile.parent.path
    private val jsScriptExecutor = JsScriptExecutor.getService(project)
    private val variableResolver = VariableResolver.getService(project)
    private val loadingRemover = httpMethod.getUserData(HttpUtils.gutterIconLoadingKey)
    private val requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget::class.java)!!
    private val request = PsiTreeUtil.getParentOfType(httpMethod, HttpRequest::class.java)!!
    private val requestBlock = PsiTreeUtil.getParentOfType(request, HttpRequestBlock::class.java)!!
    private val methodType = httpMethod.text
    private val responseHandler = PsiTreeUtil.getChildOfType(request, HttpResponseHandler::class.java)

    private val jsBeforeJsScripts = HttpUtils.getAllPreJsScripts(httpFile, requestBlock)

    private val jsAfterScriptStr = getJsScript(responseHandler)

    private val httpDashboardForm = HttpDashboardForm()

    private val version = request.version?.version ?: Version.HTTP_1_1
    private var wsRequest: WsRequest? = null

    var hasError = false

    fun getComponent(): JPanel {
        return httpDashboardForm.mainPanel
    }

    override fun startNotify() {
        super.startNotify()

        try {
            startHandleRequest()
        } catch (e: Exception) {
            destroyProcess()
            println(ExceptionUtils.getMessage(e))
            NotifyUtil.notifyError(project, "<div style='font-size:18pt'>${e.message}</div>")
            return
        }
    }

    private fun startHandleRequest() {
        jsScriptExecutor.project = project
        jsScriptExecutor.parentPath = parentPath
        jsScriptExecutor.prepareJsRequestObj()

        if (request.contentLength != null) {
            throw IllegalArgumentException("不能有 Content-Length 请求头!")
        }

        variableResolver.addFileScopeVariables(httpFile, selectedEnv, parentPath)

        val reqBody: Any? = HttpUtils.convertToReqBody(request, variableResolver, selectedEnv, parentPath)

        if (reqBody != null) {
            jsScriptExecutor.initJsRequestBody(reqBody)
        }

        val beforeJsResList = jsScriptExecutor.evalJsBeforeRequest(jsBeforeJsScripts)

        val httpReqDescList = mutableListOf<String>()
        httpReqDescList.addAll(beforeJsResList)

        val url: String = variableResolver.resolve(requestTarget.url, selectedEnv, parentPath)

        val httpHeaderFields = request.headerFieldList
        val reqHeaderMap = HttpUtils.convertToReqHeaderMap(httpHeaderFields, variableResolver, selectedEnv, parentPath)

        if (methodType == (HttpTypes.WEBSOCKET as HttpTokenType).name) {
            handleWs(url, reqHeaderMap)
            return
        }

        if (methodType == (HttpTypes.DUBBO as HttpTokenType).name) {
            handleDubbo(url, reqHeaderMap, reqBody, httpReqDescList)
            return
        }

        handleHttp(url, reqHeaderMap, reqBody, httpReqDescList)
    }

    private fun handleWs(url: String, reqHeaderMap: MutableMap<String, String>) {
        loadingRemover?.run()

        wsRequest = WsRequest(url, reqHeaderMap, this)

        httpDashboardForm.initWsResData(wsRequest, project, tabName)

        wsRequest!!.connect()
    }

    private fun handleDubbo(
        url: String,
        reqHeaderMap: MutableMap<String, String>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
    ) {
        val dubboRequest = ActionUtil.underModalProgress(project, "Tip:处理中...") {
            val module = ModuleUtil.findModuleForPsiElement(httpMethod)!!
            return@underModalProgress DubboRequest(
                tabName,
                url,
                reqHeaderMap,
                reqBody,
                httpReqDescList,
                module
            )
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

                val httpResDescList = mutableListOf("# 耗时: ${consumeTimes}ms,大小:${byteArray.size / 1024.0}kb\r\n")

                val evalJsRes = jsScriptExecutor.evalJsAfterRequest(
                    jsAfterScriptStr,
                    Pair(SimpleTypeEnum.JSON, byteArray),
                    200,
                    mutableMapOf()
                )
                if (!evalJsRes.isNullOrEmpty()) {
                    httpResDescList.add("# 后置js执行结果:\r\n")
                    httpResDescList.add("$evalJsRes")
                }

                httpResDescList.add("### $tabName\r\n")
                httpResDescList.add("DUBBO $url \r\n")
                httpResDescList.add("Content-Length: ${byteArray.size}\r\n")
                reqHeaderMap.forEach {
                    httpResDescList.add(it.key + ": " + it.value + "\r\n")
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
        reqHeaderMap: MutableMap<String, String>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
    ) {
        val start = System.currentTimeMillis()

        val httpRequestEnum = HttpRequestEnum.getInstance(httpMethod)
        val future = httpRequestEnum.execute(url, version, reqHeaderMap, reqBody, httpReqDescList, tabName)

        future.whenCompleteAsync { response, throwable ->
            runWriteActionAndWait {
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
                    mutableListOf("// status: ${response.statusCode()} 耗时: ${consumeTimes}ms 大小: $size KB\r\n")

                val evalJsRes = jsScriptExecutor.evalJsAfterRequest(
                    jsAfterScriptStr,
                    resPair,
                    response.statusCode(),
                    response.headers().map()
                )
                if (!evalJsRes.isNullOrEmpty()) {
                    httpResDescList.add("# 后置js执行结果:\r\n")
                    httpResDescList.add("$evalJsRes")
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
            }

            destroyProcess()
        }

        cancelFutureIfTerminated(future)
    }

    private fun dealResponse(httpInfo: HttpInfo, parentPath: String) {
        val requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget::class.java)!!

        val httpRequest = PsiTreeUtil.getParentOfType(requestTarget, HttpRequest::class.java)!!

        var outPutFileName: String? = null
        val httpOutputFile = PsiTreeUtil.getChildOfType(httpRequest, HttpOutputFile::class.java)
        if (httpOutputFile != null) {
            outPutFileName = httpOutputFile.outputFilePath!!.text
        }

        val saveResult = saveResToFile(outPutFileName, parentPath, httpInfo.byteArray)
        if (!saveResult.isNullOrEmpty()) {
            httpInfo.httpResDescList.add(0, saveResult)
        }

        val parentDisposer = newDisposable()
        httpDashboardForm.initHttpResContent(httpInfo, tabName, project, parentDisposer)

        val toolWindowManager = ToolWindowManager.getInstance(project)
        val myThrowable = httpDashboardForm.throwable
        hasError = myThrowable != null
        if (hasError) {
            val error = if (myThrowable is CancellationException) {
                "已中断${tabName}请求!"
            } else {
                "${tabName}请求失败,异常信息:${myThrowable.message}!"
            }
            val msg = "<div style='font-size:18pt'>$error</div>"
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, MessageType.ERROR, msg)
        } else {
            val msg = "<div style='font-size:18pt'>${tabName}请求成功!</div>"
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, MessageType.INFO, msg)
        }
    }

    private fun saveResToFile(outPutFileName: String?, parentPath: String, byteArray: ByteArray?): String? {
        if (outPutFileName == null) {
            return null
        }

        if (byteArray == null) {
            return null
        }

        val path = HttpUtils.constructFilePath(outPutFileName, parentPath)
        val file = File(path)
        if (!file.parentFile.exists()) {
            Files.createDirectories(file.toPath())
        }

        if (file.exists()) {
            try {
                Files.delete(file.toPath())
            } catch (e: Exception) {
                return "# 保存失败, ${e.message?.trim()}\r\n"
            }
        }

        val inputStream = ByteArrayInputStream(byteArray)
        Files.copy(inputStream, file.toPath())

        VirtualFileManager.getInstance().asyncRefresh(null)

        return "# 响应已保存到 ${file.normalize().absolutePath}\r\n"
    }

    private fun cancelFutureIfTerminated(future: CompletableFuture<*>) {
        CompletableFuture.runAsync {
            while (!isProcessTerminated) {
                Thread.sleep(600)
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

        runWriteActionAndWait {
            jsScriptExecutor.clearJsRequestObj()
        }

        variableResolver.clearFileScopeVariables()

        wsRequest?.abortConnect()

        val code = if (hasError) {
            1
        } else {
            0
        }
        notifyProcessTerminated(code)
    }

    override fun detachProcessImpl() {
        notifyProcessDetached()
    }

    override fun detachIsDefault(): Boolean {
        return true
    }

    override fun getProcessInput(): OutputStream? {
        return null
    }

}
