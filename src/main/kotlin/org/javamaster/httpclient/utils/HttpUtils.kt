package org.javamaster.httpclient.utils

import com.cool.request.view.tool.search.ControllerNavigationItem
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.progress.TaskInfo
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.psi.HttpPsiUtils.getNextSiblingByType
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.runconfig.HttpConfigurationType
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.springframework.util.LinkedMultiValueMap
import java.io.File
import java.net.URI
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import kotlin.jvm.optionals.getOrElse

/**
 * @author yudong
 */
object HttpUtils {
    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .disableHtmlEscaping()
        .create()

    const val READ_TIMEOUT_NAME = "readTimeout"
    const val TIMEOUT_NAME = "timeout"
    const val CONNECT_TIMEOUT_NAME = "connectTimeout"

    const val READ_TIMEOUT = 10L
    const val CONNECT_TIMEOUT = 30L

    const val HTTP_TYPE_ID = "intellijHttpClient"
    private const val VARIABLE_SIGN_END = "}}"
    val gutterIconLoadingKey: Key<Runnable?> = Key.create("GUTTER_ICON_LOADING_KEY")

    fun saveConfiguration(
        tabName: String,
        project: Project,
        selectedEnv: String?,
        httpMethod: HttpMethod,
    ): RunnerAndConfigurationSettings {
        val runManager = RunManager.getInstance(project)

        var configurationSettings = runManager.allSettings
            .firstOrNull {
                it.configuration is HttpRunConfiguration && it.configuration.name == tabName
            }

        val configNotExists = configurationSettings == null

        val httpRunConfiguration: HttpRunConfiguration
        if (configNotExists) {
            configurationSettings = runManager.createConfiguration(tabName, HttpConfigurationType::class.java)
            httpRunConfiguration = configurationSettings.configuration as HttpRunConfiguration
        } else {
            httpRunConfiguration = configurationSettings!!.configuration as HttpRunConfiguration
        }

        configurationSettings.isActivateToolWindowBeforeRun = false

        httpRunConfiguration.env = selectedEnv ?: ""
        httpRunConfiguration.httpFilePath = httpMethod.containingFile.virtualFile.path

        if (configNotExists) {
            runManager.addConfiguration(configurationSettings)
        }

        runManager.selectedConfiguration = configurationSettings

        return configurationSettings
    }

    fun getTabName(httpMethod: HttpMethod): String {
        val requestBlock = PsiTreeUtil.getParentOfType(httpMethod, HttpRequestBlock::class.java)!!
        val comment = requestBlock.comment
        if (comment != null) {
            val text = comment.text
            val tabName = text.substring(3, text.length).trim()
            if (tabName.isNotEmpty()) {
                return tabName
            }
        }

        val httpFile = requestBlock.parent as HttpFile
        val requestBlocks = httpFile.getRequestBlocks()

        for ((index, httpRequestBlock) in requestBlocks.withIndex()) {
            if (requestBlock == httpRequestBlock) {
                return "HTTP Request ▏#${index + 1}"
            }
        }

        return "HTTP Request ▏#0"
    }

    fun convertToReqHeaderMap(
        headerFields: List<HttpHeaderField>?,
        variableResolver: VariableResolver,
    ): LinkedMultiValueMap<String, String> {
        if (headerFields.isNullOrEmpty()) return LinkedMultiValueMap()

        val map = LinkedMultiValueMap<String, String>()

        headerFields.stream()
            .forEach {
                val headerName = it.headerFieldName.text
                val headerValue = it.headerFieldValue?.text ?: ""
                map.add(headerName, variableResolver.resolve(headerValue))
            }

        return map
    }

    fun resolveReqHeaderMapAgain(
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        variableResolver: VariableResolver,
    ): LinkedMultiValueMap<String, String> {
        val map = LinkedMultiValueMap<String, String>()

        reqHeaderMap.entries.stream()
            .forEach {
                val headerName = it.key
                val values = it.value

                values.forEach { value -> map.add(headerName, variableResolver.resolve(value)) }
            }

        return map
    }

    fun convertToReqBody(request: HttpRequest, variableResolver: VariableResolver): Any? {
        if (request.contentLength != null) {
            throw IllegalArgumentException("不能有 Content-Length 请求头!")
        }

        val body = request.body
        val requestMessagesGroup = body?.requestMessagesGroup
        if (requestMessagesGroup != null) {
            return handleOrdinaryContent(
                requestMessagesGroup,
                variableResolver
            )
        }

        val httpMultipartMessage = body?.multipartMessage
        if (httpMultipartMessage != null) {
            val boundary =
                request.contentTypeBoundary ?: throw IllegalArgumentException("$CONTENT_TYPE 请求头缺少 boundary!")
            return constructMultipartBody(boundary, httpMultipartMessage, variableResolver)
        }

        return null
    }

    private fun handleOrdinaryContent(
        requestMessagesGroup: HttpRequestMessagesGroup?,
        variableResolver: VariableResolver,
    ): Any? {
        if (requestMessagesGroup == null) {
            return null
        }

        var reqStr: String? = null

        val messageBody = requestMessagesGroup.messageBody
        if (messageBody != null) {
            reqStr = variableResolver.resolve(messageBody.text)
        }

        val inputFile = requestMessagesGroup.inputFile
        if (inputFile == null || inputFile.filePath == null) {
            return reqStr
        }

        val filePath = inputFile.filePath!!.text
        val path = constructFilePath(filePath, variableResolver.httpFileParentPath)

        val file = File(path)

        if (filePath.endsWith(SimpleTypeEnum.JSON.type) || filePath.endsWith(SimpleTypeEnum.XML.type)
            || filePath.endsWith(SimpleTypeEnum.TXT.type) || filePath.endsWith(SimpleTypeEnum.TEXT.type)
        ) {
            if (reqStr == null) {
                reqStr = ""
            } else {
                reqStr += "\r\n"
            }

            val str = VirtualFileUtils.readNewestContent(file)

            reqStr += variableResolver.resolve(str)

            return reqStr
        } else {
            return VirtualFileUtils.readNewestBytes(file)
        }
    }

    private fun constructMultipartBody(
        boundary: String,
        httpMultipartMessage: HttpMultipartMessage,
        variableResolver: VariableResolver,
    ): MutableList<ByteArray> {
        val byteArrays = mutableListOf<ByteArray>()

        val multipartFields = PsiTreeUtil.getChildrenOfType(httpMultipartMessage, HttpMultipartField::class.java)!!
        multipartFields
            .forEach {
                byteArrays.add("--$boundary\r\n".toByteArray(StandardCharsets.UTF_8))

                it.header.headerFieldList
                    .forEach { innerIt ->
                        val headerName = innerIt.headerFieldName.text
                        val headerValue = innerIt.headerFieldValue?.text

                        val value = if (headerValue.isNullOrEmpty()) {
                            ""
                        } else {
                            variableResolver.resolve(headerValue + "\r\n")
                        }

                        val header = "$headerName: $value"
                        byteArrays.add(header.toByteArray(StandardCharsets.UTF_8))
                    }

                byteArrays.add("\r\n".toByteArray(StandardCharsets.UTF_8))

                val content = handleOrdinaryContent(it.requestMessagesGroup, variableResolver)
                if (content is String) {
                    byteArrays.add((content + "\r\n").toByteArray(StandardCharsets.UTF_8))
                } else if (content is ByteArray) {
                    byteArrays.add(content + "\r\n".toByteArray(StandardCharsets.UTF_8))
                }
            }

        byteArrays.add("--$boundary--".toByteArray(StandardCharsets.UTF_8))

        return byteArrays
    }

    fun constructFilePath(filePath: String, parentPath: String): String {
        return if (filePath.startsWith("/") || (filePath.length > 1 && filePath[1] == ':')) {
            // 绝对路径
            filePath
        } else {
            "$parentPath/$filePath"
        }
    }

    fun convertToResHeaderDescList(response: HttpResponse<ByteArray>): MutableList<String> {
        val headerDescList = mutableListOf<String>()
        val headers = response.headers()
        headers.map()
            .forEach { (t, u) ->
                u.forEach {
                    headerDescList.add("$t: $it\r\n")
                }
            }
        headerDescList.add("\r\n")
        return headerDescList
    }

    fun convertToResPair(response: HttpResponse<ByteArray>): Pair<SimpleTypeEnum, ByteArray> {
        val resBody = response.body()
        val resHeaders = response.headers()
        val contentType = resHeaders.firstValue(CONTENT_TYPE).getOrElse { ContentType.TEXT_PLAIN.mimeType }

        if (contentType.contains(SimpleTypeEnum.JSON.type)) {
            val jsonStr = String(resBody, StandardCharsets.UTF_8)
            try {
                val jsonElement = gson.fromJson(jsonStr, JsonElement::class.java)
                val jsonStrPretty = gson.toJson(jsonElement)
                return Pair(SimpleTypeEnum.JSON, jsonStrPretty.toByteArray(StandardCharsets.UTF_8))
            } catch (e: JsonSyntaxException) {
                return Pair(SimpleTypeEnum.JSON, resBody)
            }
        }

        if (contentType.contains(SimpleTypeEnum.HTML.type)) {
            return Pair(SimpleTypeEnum.HTML, resBody)
        }

        if (contentType.contains(SimpleTypeEnum.XML.type)) {
            return Pair(SimpleTypeEnum.XML, resBody)
        }

        if (contentType.contains(SimpleTypeEnum.TEXT.type)) {
            return Pair(SimpleTypeEnum.TEXT, resBody)
        }

        if (contentType.contains(SimpleTypeEnum.IMAGE.type)) {
            return Pair(SimpleTypeEnum.IMAGE, resBody)
        }

        return Pair(SimpleTypeEnum.STREAM, resBody)
    }

    fun getJsScript(httpResponseHandler: HttpResponseHandler?): HttpScriptBody? {
        if (httpResponseHandler == null) {
            return null
        }

        return httpResponseHandler.responseScript.scriptBody
    }

    fun resolveFileGlobalVariable(variableName: String, httpFile: PsiFile): PsiElement? {
        val globalVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable::class.java)

        val element = globalVariables
            .map {
                val firstChild = it.globalVariableName.firstChild
                val psiElement = getNextSiblingByType(firstChild, HttpTypes.GLOBAL_NAME, false) ?: return@map null
                if (psiElement.text == variableName) {
                    return@map psiElement.parent.parent
                } else {
                    return@map null
                }
            }
            .filterNotNull()
            .firstOrNull()

        if (element == null) {
            return null
        }

        return element
    }

    fun getAllPreJsScripts(httpFile: PsiFile, httpRequestBlock: HttpRequestBlock): List<HttpScriptBody> {
        val scripts = mutableListOf<HttpScriptBody>()

        val globalScript = getGlobalJsScript(httpFile)
        if (globalScript != null) {
            scripts.add(globalScript)
        }

        val preJsScript = getPreJsScript(httpRequestBlock)
        if (preJsScript != null) {
            scripts.add(preJsScript)
        }

        return scripts
    }

    fun getAllPostJsScripts(httpFile: PsiFile): List<HttpScriptBody> {
        val handlers = PsiTreeUtil.findChildrenOfType(httpFile, HttpResponseHandler::class.java)

        return handlers
            .mapNotNull {
                getJsScript(it)
            }
    }

    fun getDirectionCommentParamMap(httpRequestBlock: HttpRequestBlock): Map<String, String> {
        val list = PsiTreeUtil.getChildrenOfTypeAsList(httpRequestBlock, HttpDirectionComment::class.java)
        val map = mutableMapOf<String, String>()
        list.forEach {
            val name = it.directionName?.text ?: return@forEach
            map[name] = it.directionValue?.text ?: ""
        }
        return map
    }

    private fun getGlobalJsScript(httpFile: PsiFile): HttpScriptBody? {
        val globalHandler = PsiTreeUtil.getChildOfType(httpFile, HttpGlobalHandler::class.java) ?: return null
        return globalHandler.globalScript.scriptBody
    }

    private fun getPreJsScript(httpRequestBlock: HttpRequestBlock): HttpScriptBody? {
        val preRequestHandler = httpRequestBlock.preRequestHandler ?: return null
        return preRequestHandler.preRequestScript.scriptBody
    }

    fun getOriginalFile(requestTarget: HttpRequestTarget): VirtualFile? {
        val virtualFile = PsiUtil.getVirtualFile(requestTarget)
        if (!isFileInIdeaDir(virtualFile)) {
            return virtualFile
        }

        val httpMethod = PsiTreeUtil.getPrevSiblingOfType(requestTarget, HttpMethod::class.java) ?: return null

        val tabName = getTabName(httpMethod)

        val runManager = RunManager.getInstance(requestTarget.project)
        val configurationSettings = runManager.allSettings
            .firstOrNull {
                it.configuration is HttpRunConfiguration && it.configuration.name == tabName
            }
        if (configurationSettings == null) {
            return null
        }

        val httpRunConfiguration = configurationSettings.configuration as HttpRunConfiguration

        return VfsUtil.findFileByIoFile(File(httpRunConfiguration.httpFilePath), true)
    }

    fun getOriginalModule(requestTarget: HttpRequestTarget): Module? {
        val project = requestTarget.project

        val virtualFile = getOriginalFile(requestTarget) ?: return null

        return ModuleUtilCore.findModuleForFile(virtualFile, project)
    }

    fun getSearchTxtInfo(requestTarget: HttpRequestTarget, httpFileParentPath: String): Pair<String, TextRange>? {
        val project = requestTarget.project

        val url = requestTarget.text

        val start: Int
        val bracketIdx = url.indexOf(VARIABLE_SIGN_END)
        start = if (bracketIdx != -1) {
            bracketIdx + 2
        } else {
            val envFileService = EnvFileService.getService(project)
            val selectedEnv = HttpEditorTopForm.getSelectedEnv(project)
            val contextPath = envFileService.getEnvValue("contextPath", selectedEnv, httpFileParentPath)

            val tmpIdx: Int
            val uri: URI
            try {
                uri = URI(url)
                tmpIdx = if (contextPath == null) {
                    url.indexOf(uri.path)
                } else {
                    url.indexOf(contextPath) + contextPath.length
                }
            } catch (e: Exception) {
                return null
            }
            tmpIdx
        }

        if (start == -1) {
            return null
        }

        val idx = url.lastIndexOf("?")
        val end = if (idx == -1) {
            url.length
        } else {
            idx
        }

        if (end < start) {
            return null
        }

        val textRange = TextRange(start, end)
        val searchTxt = url.substring(start, end)
        return Pair(searchTxt, textRange)
    }

    fun isFileInIdeaDir(virtualFile: VirtualFile?): Boolean {
        return virtualFile?.name?.startsWith("tmp") == true
    }

    fun getTargetHttpMethod(httpFilePath: String, runConfigName: String, project: Project): HttpMethod? {
        val virtualFile = VfsUtil.findFileByIoFile(File(httpFilePath), false) ?: return null

        val psiFile = PsiUtil.getPsiFile(project, virtualFile)
        val httpMethods = PsiTreeUtil.findChildrenOfType(psiFile, HttpMethod::class.java)

        return httpMethods.firstOrNull {
            val tabName = getTabName(it)
            runConfigName == tabName
        }
    }

    fun resolveFilePath(path: String, httpFileParentPath: String, project: Project): PsiElement? {
        val filePath = constructFilePath(path, httpFileParentPath)

        val file = File(filePath)
        if (!file.exists()) {
            return null
        }

        val virtualFile = VfsUtil.findFileByIoFile(file, true)!!

        if (file.isDirectory) {
            return PsiManager.getInstance(project).findDirectory(virtualFile)!!
        }

        return PsiUtil.getPsiFile(project, virtualFile)
    }

    @Suppress("DEPRECATION")
    fun createActionEvent(): AnActionEvent {
        @Suppress("removal")
        return AnActionEvent(
            null,
            DataManager.getInstance().dataContext,
            "",
            Presentation(""),
            ActionManager.getInstance(),
            1
        )
    }

    fun createProcessIndicator(title: String, project: Project): BackgroundableProcessIndicator {
        return BackgroundableProcessIndicator(project, object : TaskInfo {
            override fun getTitle(): String {
                return title
            }

            override fun getCancelText(): String {
                return "Tip:正在取消......"
            }

            override fun getCancelTooltipText(): String {
                return "Tip:取消中......"
            }

            override fun isCancellable(): Boolean {
                return true
            }
        })
    }

    fun findControllerPsiMethods(
        navigationItem: ControllerNavigationItem,
        module: Module,
    ): Array<out PsiMethod> {
        val controllerFullClassName = navigationItem.javaClassName
        val controllerMethodName = navigationItem.methodName

        val controllerPsiCls = JavaPsiFacade.getInstance(module.project)
            .findClass(
                controllerFullClassName,
                GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
            ) ?: return arrayOf()

        val psiMethods = controllerPsiCls.findMethodsByName(controllerMethodName, false)
        if (psiMethods.isEmpty()) {
            return arrayOf()
        }

        return psiMethods
    }

    fun findControllerNavigationItem(controllers: MutableList<Any>, searchTxt: String): ControllerNavigationItem {
        return if (controllers.size == 1) {
            controllers[0] as ControllerNavigationItem
        } else {
            val urlMap = controllers.groupBy {
                val navigationItem = it as ControllerNavigationItem
                navigationItem.url
            }
            val itemList = urlMap[searchTxt]
            if (itemList.isNullOrEmpty()) {
                controllers[0] as ControllerNavigationItem
            } else {
                itemList[0] as ControllerNavigationItem
            }
        }
    }

}
