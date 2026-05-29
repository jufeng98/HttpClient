package org.javamaster.httpclient.processHandler

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.application
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.consts.HttpConsts.Companion.FAILED
import org.javamaster.httpclient.consts.HttpConsts.Companion.SUCCESS
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvMap
import org.javamaster.httpclient.handler.RunFileHandler
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.model.HttpReqInfo
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.CookieFile
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.*
import java.io.OutputStream
import java.net.http.HttpClient.Version
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import javax.swing.JPanel

/**
 * @author yudong
 */
abstract class ProcessHandlerBase(val httpMethod: HttpMethod, private val selectedEnv: String?) : ProcessHandler() {
    var httpStatus: Int? = null
    var costTimes: Long? = null
    var finishedTime = Long.MAX_VALUE
    var hasError = false

    var tabName = HttpUtils.getTabName(httpMethod)
    var project = httpMethod.project

    protected lateinit var httpFile: HttpFile
    protected lateinit var parentPath: String
    protected lateinit var jsExecutor: JsExecutor
    protected lateinit var variableResolver: VariableResolver
    protected lateinit var requestTarget: HttpRequestTarget
    protected lateinit var rawUrl: String
    protected lateinit var request: HttpRequest
    protected lateinit var requestBlock: HttpRequestBlock
    protected lateinit var methodType: HttpRequestEnum
    protected lateinit var version: Version
    protected lateinit var preJsFiles: List<PreJsFile>
    protected lateinit var jsListBeforeReq: List<HttpScriptBody>
    protected lateinit var paramMap: Map<String, String>

    protected var loadingRemover: Runnable? = null
    protected var responseHandler: HttpResponseHandler? = null
    protected var rawBody: String? = null
    protected var outPutFilePath: String? = null
    protected var jsAfterReq: HttpScriptBody? = null
    protected var cookiesPsiFile: CookieFile? = null

    protected val httpDashboardForm by lazy {
        HttpDashboardForm(tabName, project)
    }


    override fun startNotify() {
        super.startNotify()

        application.executeOnPooledThread {
            val cookiesVirtualFile = initCookiesVirtualFile()

            runReadAction {
                try {
                    if (cookiesVirtualFile != null) {
                        cookiesPsiFile = PsiUtil.getPsiFile(project, cookiesVirtualFile) as CookieFile
                    }

                    initStatus()

                    val finished = downloadPreJsNpmFiles()
                    if (!finished) {
                        destroyProcess()
                        return@runReadAction
                    }

                    initPreJsFiles()

                    val otherFinished = downloadOtherFiles()
                    if (!otherFinished) {
                        destroyProcess()
                        return@runReadAction
                    }

                    startProcess()
                } catch (e: Exception) {
                    handleException(e)
                }
            }
        }
    }

    open fun downloadOtherFiles(): Boolean {
        return true
    }

    fun initCookiesVirtualFile(): VirtualFile? {
        val cookiesFile = CookieUtils.createCookiesFileIfNotExists(project) ?: return null

        return VfsUtil.findFileByIoFile(cookiesFile, true)
    }

    private fun initStatus() {
        httpFile = httpMethod.containingFile as HttpFile
        parentPath = httpFile.virtualFile.parent.path
        methodType = HttpRequestEnum.getInstance(httpMethod.text)
        jsExecutor = JsExecutor(project, parentPath, tabName)
        variableResolver = VariableResolver(jsExecutor, httpFile, selectedEnv, project)
        loadingRemover = httpMethod.getUserData(HttpConsts.gutterIconLoadingKey)
        requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget::class.java)!!
        rawUrl = requestTarget.url
        request = PsiTreeUtil.getParentOfType(httpMethod, HttpRequest::class.java)!!
        requestBlock = PsiTreeUtil.getParentOfType(request, HttpRequestBlock::class.java)!!
        responseHandler = PsiTreeUtil.getChildOfType(request, HttpResponseHandler::class.java)
        outPutFilePath = PsiTreeUtil.getChildOfType(request, HttpOutputFile::class.java)?.filePath?.text
        version = request.version?.version ?: Version.HTTP_1_1
        rawBody = request.body?.text

        preJsFiles = HttpUtils.getPreJsFiles(httpFile, false, true)

        jsListBeforeReq = MyPsiUtils.getAllPreJsScripts(httpFile, requestBlock)

        jsAfterReq = MyPsiUtils.getJsScript(responseHandler)

        paramMap = MyPsiUtils.getReqDirectionCommentParamMap(requestBlock)
    }

    /**
     * 下载 npm 依赖的压缩包并解压
     */
    fun downloadPreJsNpmFiles(): Boolean {
        val preFilePair = preJsFiles.partition { it.urlFile != null }

        val npmFiles = preFilePair.first

        if (npmFiles.isEmpty()) {
            return true
        }

        val npmFilesNotDownloaded = NpmJsUtils.jsLibrariesNotDownloaded(npmFiles)
        if (npmFilesNotDownloaded.isEmpty()) {
            return true
        }

        runInEdt {
            NpmJsUtils.downloadAsyncInEdt(project, npmFilesNotDownloaded)

            httpDashboardForm.resetDashboardForm()
        }

        return false
    }

    /**
     * 1.初始化 npm 依赖的 js 文件(通过读取 package.json 找到入口文件实现);
     * 2.初始化所有 js 文件的 VirtualFile;
     * 3.初始化所有 js 文件的内容
     */
    fun initPreJsFiles() {
        val preFilePair = preJsFiles.partition { it.urlFile != null }

        val npmFiles = preFilePair.first

        NpmJsUtils.initAndCacheNpmJsLibrariesFile(npmFiles, project)

        NpmJsUtils.initJsLibrariesVirtualFile(preJsFiles)

        ReqUtils.initPreJsFilesContent(preJsFiles, project, httpFile)
    }

    abstract fun startProcess()

    fun createHttpReqInfo(): HttpReqInfo {
        ReqUtils.initPreJsFilesContent(preJsFiles, project, httpFile)

        var reqBody = HttpUtils.convertToReqBody(request, variableResolver, paramMap)

        val environment = getEnvMap(project, false)

        val fileCookies = CookieUtils.getValidFileCookieMap(cookiesPsiFile)

        return HttpReqInfo(reqBody, environment, preJsFiles, fileCookies)
    }

    fun resolveAndHandleUrl(): String {
        var url = variableResolver.resolve(rawUrl)

        return ReqUtils.handleUrl(url)
    }

    fun executePreJs(
        url: String,
        reqInfo: HttpReqInfo,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
    ): List<String> {
        jsExecutor.initJsRequestObj(
            url,
            rawUrl,
            rawBody,
            reqInfo,
            methodType,
            reqHeaderMap,
            selectedEnv,
            variableResolver.fileScopeVariableMap
        )

        return jsExecutor.evalJsBeforeRequest(reqInfo.preJsFiles, jsListBeforeReq)
    }

    fun dealResponse(httpInfo: HttpInfo, parentPath: String) {
        finishedTime = System.currentTimeMillis()

        if (outPutFilePath != null && httpInfo.byteArray != null) {
            var path = variableResolver.resolve(outPutFilePath!!)
            path = HttpUtils.constructFilePath(path, parentPath)

            val saveResult = ResUtils.saveResToFile(path, httpInfo.byteArray)

            httpInfo.httpResDescList.add(0, saveResult)
        }

        runInEdt {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow(ToolWindowId.SERVICES)

            val content = toolWindow!!.contentManager.getContent(getComponent())
            if (content != null) {
                content.setDisposer(httpDashboardForm)
            } else {
                Disposer.register(Disposer.newDisposable(), httpDashboardForm)
            }

            WriteAction.run<Exception> {
                httpDashboardForm.initHttpResContent(httpInfo, paramMap.containsKey(ParamEnum.NO_LOG.param))
            }

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
        }
    }

    fun handleException(e: Exception) {
        e.printStackTrace()

        runInEdt {
            httpDashboardForm.resetDashboardForm()
            NotifyUtil.notifyError(project, "<div style='font-size:13pt'>${e}</div>")
        }

        destroyProcess()
    }

    fun switchToEdt(runnable: Runnable) {
        runInEdt {
            try {
                runnable.run()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun cancelFutureIfTerminated(future: CompletableFuture<*>) {
        CompletableFuture.runAsync {
            while (!isProcessTerminated && !RunFileHandler.isInterrupted()) {
                Thread.sleep(600)
            }

            runInEdt {
                loadingRemover?.run()
            }

            future.cancel(true)
        }
    }

    fun getComponent(): JPanel {
        return httpDashboardForm.mainPanel
    }

    override fun destroyProcessImpl() {
        val code = if (hasError) FAILED else SUCCESS

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
        internal val requestRunningSet = mutableSetOf<String>()

        fun isRunning(tabName: String): Boolean {
            return requestRunningSet.contains(tabName)
        }

        fun createProcessHandler(httpMethod: HttpMethod, selectedEnv: String?): ProcessHandlerBase {
            val requestEnum = HttpRequestEnum.getInstance(httpMethod.text)

            return when (requestEnum) {
                HttpRequestEnum.WEBSOCKET -> WebSocketProcessHandler(httpMethod, selectedEnv)

                HttpRequestEnum.DUBBO -> DubboProcessHandler(httpMethod, selectedEnv)

                HttpRequestEnum.MOCK_SERVER -> MockServerProcessHandler(httpMethod, selectedEnv)

                else -> HttpProcessHandler(httpMethod, selectedEnv)
            }
        }
    }
}
