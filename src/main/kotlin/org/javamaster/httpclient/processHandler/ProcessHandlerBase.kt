package org.javamaster.httpclient.processHandler

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runInEdt
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.application
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.consts.HttpConsts.Companion.FAILED
import org.javamaster.httpclient.consts.HttpConsts.Companion.SUCCESS
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvMap
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.model.HttpReqInfo
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.service.RunHttpFileService
import org.javamaster.httpclient.ui.HttpDashboardForm
import org.javamaster.httpclient.utils.*
import org.javamaster.httpclient.utils.HttpUtils.runReadAction
import java.io.OutputStream
import java.net.http.HttpClient.Version
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import javax.swing.JPanel

/**
 * @author yudong
 */
abstract class ProcessHandlerBase(val httpMethod: HttpMethod, private val selectedEnv: String?) : ProcessHandler(),
    Disposable {
    var httpStatus: Int? = null
    var costTimes: Long? = null
    var future: CompletableFuture<*>? = null
    var hasError = false

    var tabName = HttpUtils.getTabName(httpMethod)
    var project = httpMethod.project
    protected val httpFile = httpMethod.containingFile as HttpFile
    protected val httpDocument = PsiDocumentManager.getInstance(project).getDocument(httpFile)!!

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
    protected var requestFinished: Consumer<Int>? = null
    protected var responseHandler: HttpResponseHandler? = null
    protected var rawBody: String? = null
    protected var outPutFilePath: String? = null
    protected var jsAfterReq: HttpScriptBody? = null

    protected val httpDashboardForm by lazy {
        HttpDashboardForm(tabName, this, project)
    }


    override fun startNotify() {
        super.startNotify()

        application.executeOnPooledThread {
            try {
                CookieUtils.createCookiesFileIfNotExists(project)

                initStatus()

                val finished = downloadPreJsNpmFiles()
                if (!finished) {
                    detachProcess()

                    return@executeOnPooledThread
                }

                initPreJsFiles()

                val otherFinished = downloadOtherFiles()
                if (!otherFinished) {
                    detachProcess()

                    return@executeOnPooledThread
                }

                startProcess()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun initStatus() {
        parentPath = httpFile.virtualFile.parent.path
        methodType = HttpRequestEnum.getInstance(httpMethod.text)
        jsExecutor = JsExecutor(project, parentPath, tabName)
        loadingRemover = httpMethod.getUserData(HttpConsts.gutterIconLoadingKey)
        requestFinished = httpMethod.getUserData(HttpConsts.requestFinishedKey)

        runReadAction {
            preJsFiles = HttpUtils.getPreJsFiles(httpFile, false, true)
            variableResolver = VariableResolver(jsExecutor, httpFile, selectedEnv, project)
            requestTarget = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpRequestTarget::class.java)!!
            rawUrl = requestTarget.url
            request = PsiTreeUtil.getParentOfType(httpMethod, HttpRequest::class.java)!!
            requestBlock = PsiTreeUtil.getParentOfType(request, HttpRequestBlock::class.java)!!
            responseHandler = PsiTreeUtil.getChildOfType(request, HttpResponseHandler::class.java)
            outPutFilePath = PsiTreeUtil.getChildOfType(request, HttpOutputFile::class.java)?.filePath?.text
            rawBody = request.body?.text
            version = request.version?.version ?: Version.HTTP_1_1

            jsListBeforeReq = MyPsiUtils.getAllPreJsScripts(httpFile, requestBlock)

            jsAfterReq = MyPsiUtils.getJsScript(responseHandler)

            paramMap = MyPsiUtils.getReqDirectionCommentParamMap(requestBlock)
        }
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

        NpmJsUtils.downloadAsync(project, npmFilesNotDownloaded, httpFile.virtualFile.path)

        runInEdt { httpDashboardForm.resetDashboardForm() }

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

    open fun downloadOtherFiles(): Boolean {
        return true
    }

    abstract fun startProcess()

    fun createHttpReqInfo(): HttpReqInfo {
        var reqBody = HttpUtils.convertToReqBody(request, variableResolver, paramMap)

        val environment = getEnvMap(project, false)

        val fileCookies = CookieUtils.getValidFileCookieMap(project)

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
            url, rawUrl, rawBody, reqInfo, methodType, reqHeaderMap,
            selectedEnv, variableResolver.fileScopeVariableMap
        )

        return jsExecutor.evalJsBeforeRequest(reqInfo.preJsFiles, jsListBeforeReq, httpFile.name, httpDocument)
    }

    fun dealResponse(httpInfo: HttpInfo, parentPath: String) {
        if (outPutFilePath != null && httpInfo.byteArray != null) {
            var path = variableResolver.resolve(outPutFilePath!!)

            path = HttpUtils.constructFilePath(path, parentPath)

            val saveResult = ResUtils.saveResBodyToFile(path, httpInfo.byteArray)

            httpInfo.httpResDescList.add(0, saveResult)
        }

        httpDashboardForm.initHttpResContent(httpInfo, paramMap.containsKey(ParamEnum.NO_LOG.param))

        val virtualFile = httpFile.virtualFile
        val isRunFile = virtualFile.getUserData(HttpConsts.runFileKey) == true

        val myThrowable = httpInfo.httpException
        if (myThrowable != null) {
            myThrowable.printStackTrace()

            val error = if (myThrowable is CancellationException || myThrowable.cause is CancellationException) {
                nls("req.interrupted", tabName)
            } else {
                nls("req.failed", tabName, myThrowable)
            }

            if (!isRunFile) {
                NotifyUtil.notifyError(project, error)
            }
        } else {
            val msg = "$tabName ${nls("request.success")}!"

            if (!isRunFile) {
                NotifyUtil.notifyInfo(project, msg)
            }
        }
    }

    fun handleException(e: Exception) {
        logWarn("处理出错了", e)

        runInEdt { httpDashboardForm.resetDashboardForm() }

        NotifyUtil.notifyError(project, "$e")

        detachProcess()
    }

    fun getComponent(): JPanel {
        return httpDashboardForm.mainPanel
    }

    override fun destroyProcessImpl() {
        if (future?.isDone == false) {
            future?.cancel(true)

            val virtualFile = httpFile.virtualFile
            if (virtualFile.getUserData(HttpConsts.runFileKey) == true) {
                project.getService(RunHttpFileService::class.java).stopRunning(virtualFile)
            }
        }

        val code = if (hasError) FAILED else SUCCESS

        requestFinished?.accept(code)

        httpMethod.putUserData(HttpConsts.gutterIconLoadingKey, null)

        httpMethod.putUserData(HttpConsts.requestFinishedKey, null)

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

    override fun dispose() {

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
