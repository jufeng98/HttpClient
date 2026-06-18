package org.javamaster.httpclient.processHandler

import com.intellij.execution.process.ProcessHandler
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.notification.NotificationAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainTextFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.application
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.consts.HttpConsts.Companion.FAILED
import org.javamaster.httpclient.consts.HttpConsts.Companion.SUCCESS
import org.javamaster.httpclient.consts.HttpConsts.Companion.VAR_BRACE_END
import org.javamaster.httpclient.consts.HttpConsts.Companion.VAR_BRACE_START
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvMap
import org.javamaster.httpclient.exception.BodyUnresolvedVariableException
import org.javamaster.httpclient.exception.HeaderUnresolvedVariableException
import org.javamaster.httpclient.exception.JsScriptException
import org.javamaster.httpclient.exception.UrlUnresolvedVariableException
import org.javamaster.httpclient.fake.FakeUnsolvedVariableElement
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
import org.javamaster.httpclient.utils.HttpUtils.constructFilePath
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
    var hasReqError = false
    var jsScriptException: JsScriptException? = null

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

        var results: List<String>
        try {
            results = jsExecutor.evalJsBeforeRequest(reqInfo.preJsFiles, jsListBeforeReq, httpFile.name, httpDocument)
        } catch (e: JsScriptException) {
            jsScriptException = e

            results = e.list
        }

        return results
    }

    fun dealPreJsErrorResponse(httpReqDescList: MutableList<String>) {
        val httpInfo = HttpInfo(httpReqDescList, mutableListOf(), null, null, jsScriptException)

        dealResponse(httpInfo, parentPath)

        detachProcess()
    }

    fun dealResponse(httpInfo: HttpInfo, parentPath: String) {
        if (outPutFilePath != null && httpInfo.byteArray != null) {
            var path = variableResolver.resolve(outPutFilePath!!)

            path = constructFilePath(path, parentPath)

            val saveResult = ResUtils.saveResBodyToFile(path, httpInfo.byteArray)

            httpInfo.httpResDescList.add(0, saveResult)
        }

        httpDashboardForm.initHttpResContent(httpInfo, paramMap.containsKey(ParamEnum.NO_LOG.param))

        val virtualFile = httpFile.virtualFile
        val isRunFile = virtualFile.getUserData(HttpConsts.runFileKey) == true

        val myThrowable = httpInfo.httpException ?: jsScriptException
        if (myThrowable != null) {
            logWarn("出错了", myThrowable)

            val error = if (myThrowable is CancellationException || myThrowable.cause is CancellationException) {
                nls("req.interrupted", tabName)
            } else {
                if (myThrowable is JsScriptException) {
                    nls("handle.failed", tabName, myThrowable.cause!!)
                } else {
                    nls("req.failed", tabName, myThrowable)
                }
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
        hasReqError = true

        if (jsScriptException != null) {
            logWarn("出错了", jsScriptException!!.cause)
            NotifyUtil.notifyCornerError(project, nls("handle.failed", tabName, jsScriptException!!.cause!!))
        }

        logWarn("处理出错了", e)

        runInEdt { httpDashboardForm.resetDashboardForm() }

        runReadAction {
            if (e is UrlUnresolvedVariableException) {
                urlActionNotify(e.url)
            } else if (e is BodyUnresolvedVariableException) {
                bodyActionNotify(e.variableName)
            } else if (e is HeaderUnresolvedVariableException) {
                headerActionNotify(e.variableName)
            } else {
                NotifyUtil.notifyCornerError(project, nls("handle.failed", tabName, e))
            }
        }

        detachProcess()
    }

    private fun urlActionNotify(url: String) {
        val idxStart = url.indexOf(VAR_BRACE_START)
        if (idxStart == -1) {
            actionNotify("unknown", httpMethod)
            return
        }

        val idxEnd = url.indexOf(VAR_BRACE_END, idxStart)
        if (idxEnd == -1) {
            actionNotify("unknown", httpMethod)
            return
        }

        val variableName = url.substring(idxStart + VAR_BRACE_START.length, idxEnd)

        val varElement = PsiTreeUtil.findChildrenOfType(requestTarget, HttpVariableName::class.java)
            .firstOrNull { it.text == variableName }
        if (varElement == null) {
            actionNotify(variableName, httpMethod)
            return
        }

        actionNotify(variableName, varElement)
    }

    private fun headerActionNotify(variableName: String) {
        val varElement = PsiTreeUtil.findChildrenOfType(request.header, HttpVariableName::class.java)
            .firstOrNull { it.text == variableName }
        if (varElement == null) {
            actionNotify(variableName, httpMethod)
            return
        }

        actionNotify(variableName, varElement)
    }

    private fun bodyActionNotify(variableName: String) {
        val body = request.body!!
        val requestMessagesGroup = body.requestMessagesGroup
        val multipartMessage = body.multipartMessage

        val messageBody = requestMessagesGroup?.messageBody
        val inputFile = requestMessagesGroup?.inputFile

        if (messageBody != null) {
            val injectedPsiFiles = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(messageBody)
            val injectedPsiTextFiles = injectedPsiFiles?.filter { it.first is PsiPlainTextFile }
            if (injectedPsiTextFiles == null) {
                actionNotify(variableName, httpMethod)
                return
            }

            if (injectedPsiFiles.size != injectedPsiTextFiles.size) {
                val element = injectedPsiTextFiles.firstOrNull { it.first.text == "{{$variableName}}" }?.first
                if (element != null) {
                    actionNotify(variableName, element)
                    return
                }
            }

            val idx = messageBody.text.indexOf("{{$variableName}}")
            if (idx == -1) {
                actionNotify(variableName, httpMethod)
                return
            }

            val element = messageBody
            val offset = messageBody.textOffset + idx + VAR_BRACE_START.length

            actionNotify(variableName, httpFile.virtualFile, httpDocument, element, offset)
        } else if (inputFile != null) {
            var filePathStr = variableResolver.resolve(inputFile.filePath!!.text)
            val path = constructFilePath(filePathStr, variableResolver.httpFileParentPath)
            var file = LocalFileSystem.getInstance().findFileByPath(path)!!
            val doc = FileDocumentManager.getInstance().getDocument(file)!!

            val idx = doc.text.indexOf("{{$variableName}}")
            if (idx == -1) {
                actionNotify(variableName, httpMethod)
                return
            }

            val element = inputFile
            val offset = idx + VAR_BRACE_START.length

            actionNotify(variableName, file, doc, element, offset)
        } else if (multipartMessage != null) {
            val varElement = PsiTreeUtil.findChildrenOfType(multipartMessage, HttpVariableName::class.java)
                .firstOrNull { it.text == variableName }
            if (varElement != null) {
                actionNotify(variableName, varElement)
                return
            }

            val idx = multipartMessage.text.indexOf("{{$variableName}}")
            if (idx == -1) {
                actionNotify(variableName, httpMethod)
                return
            }

            val element = multipartMessage
            val offset = multipartMessage.textOffset + idx + VAR_BRACE_START.length

            actionNotify(variableName, httpFile.virtualFile, httpDocument, element, offset)
        }
    }

    private fun actionNotify(
        variableName: String,
        file: VirtualFile,
        doc: Document,
        psiElement: PsiElement,
        offset: Int,
    ) {
        val lineNumber = doc.getLineNumber(offset)

        val lineStartOffset = doc.getLineStartOffset(lineNumber)
        val column = offset - lineStartOffset

        val element = FakeUnsolvedVariableElement(offset, variableName, file, psiElement)

        val content = nls("goto.detail", "${file.name}:${lineNumber + 1}:${column + 1}")

        val actionJumpTo = NotificationAction.createSimple(content) {
            runInEdt { if (element.isValid) (element as Navigatable).navigate(true) }
        }

        NotifyUtil.notifyCornerError(
            project, nls("invalid.request", variableName, tabName), actionJumpTo
        )
    }

    private fun actionNotify(variableName: String, psiElement: PsiElement) {
        val file = psiElement.containingFile!!
        val doc = PsiDocumentManager.getInstance(project).getDocument(file)!!

        val offset = psiElement.textOffset
        val lineNumber = doc.getLineNumber(offset)

        val lineStartOffset = doc.getLineStartOffset(lineNumber)
        val column = offset - lineStartOffset

        val content = nls("goto.detail", "${file.name}:${lineNumber + 1}:${column + 1}")

        val actionJumpTo = NotificationAction.createSimple(content) {
            runInEdt { if (psiElement.isValid) (psiElement as Navigatable).navigate(true) }
        }

        NotifyUtil.notifyCornerError(
            project, nls("invalid.request", variableName, tabName), actionJumpTo
        )
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

        val code = if (hasReqError || jsScriptException != null) FAILED else SUCCESS

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
