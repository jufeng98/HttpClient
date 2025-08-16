package org.javamaster.httpclient.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.ide.impl.ProjectUtil
import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.*
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.refactoring.rename.RenameProcessor
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.entity.ContentType
import org.intellij.markdown.html.urlEncode
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.adapter.DateTypeAdapter
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvEleLiteral
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvJsonProperty
import org.javamaster.httpclient.factory.HttpPsiFactory.createGlobalVariable
import org.javamaster.httpclient.factory.JsonPsiFactory.createBoolProperty
import org.javamaster.httpclient.factory.JsonPsiFactory.createNumberProperty
import org.javamaster.httpclient.factory.JsonPsiFactory.createStringProperty
import org.javamaster.httpclient.js.JsExecutor.Companion.setGlobalVariable
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.psi.HttpPsiUtils.getNextSiblingByType
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.runconfig.HttpConfigurationType
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.ui.HttpEditorTopForm
import java.io.File
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*
import javax.swing.Icon
import kotlin.jvm.optionals.getOrElse

/**
 * @author yudong
 */
object HttpUtils {
    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .disableHtmlEscaping()
        .registerTypeAdapter(Date::class.java, DateTypeAdapter)
        .create()

    const val REQUEST_BODY_ANNO_NAME = "org.springframework.web.bind.annotation.RequestBody"
    const val API_OPERATION_ANNO_NAME = "io.swagger.annotations.ApiOperation"
    const val API_MODEL_PROPERTY_ANNO_NAME = "io.swagger.annotations.ApiModelProperty"
    const val CR_LF = "\r\n"

    const val READ_TIMEOUT = 3600L
    const val CONNECT_TIMEOUT = 30L
    const val TIMEOUT = 10_000

    const val RES_SIZE_LIMIT = 2 * 1024 * 1024

    const val HTTP_TYPE_ID = "intellijHttpClient"
    const val WEB_BOUNDARY = "boundary"

    private const val VARIABLE_SIGN_END = "}}"

    val gutterIconLoadingKey: Key<Runnable?> = Key.create("GUTTER_ICON_LOADING_KEY")
    val requestFinishedKey: Key<Int> = Key.create("REQUEST_FINISHED_KEY")

    const val SUCCESS = 0
    const val FAILED = 1

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

    fun getInjectHost(jsonString: JsonStringLiteral, project: Project): HttpMessageBody? {
        if (!jsonString.isPropertyName) {
            return null
        }

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(jsonString)
        if (injectionHost !is HttpMessageBody) {
            return null
        }

        return injectionHost
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

    fun encodeUrl(url: String): String {
        val split = url.split("?")
        if (split.size == 1) {
            return url
        }

        return split[0] + "?" + encodeQueryParam(split[1])
    }

    private fun encodeQueryParam(queryParam: String): String {
        val split = queryParam.split("&")

        return split.joinToString("&") {
            val list = it.split("=")
            urlEncode(list[0]) + "=" + urlEncode(list[1])
        }
    }

    fun convertToReqBody(
        request: HttpRequest,
        variableResolver: VariableResolver,
        paramMap: Map<String, String>,
    ): Any? {
        if (request.contentLength != null) {
            throw IllegalArgumentException(NlsBundle.nls("content.length.error"))
        }

        val body = request.body

        val requestMessagesGroup = body?.requestMessagesGroup
        if (requestMessagesGroup != null) {
            return handleOrdinaryContent(
                requestMessagesGroup,
                variableResolver,
                request.header,
                request.contentType,
                paramMap
            )
        }

        val httpMultipartMessage = body?.multipartMessage
        if (httpMultipartMessage != null) {
            val boundary = request.contentTypeBoundary
                ?: throw IllegalArgumentException(NlsBundle.nls("lack.boundary", CONTENT_TYPE))

            return constructMultipartBody(boundary, httpMultipartMessage, variableResolver, paramMap)
        }

        return null
    }

    private fun isTxtContentType(header: HttpHeader?): Boolean {
        if (header == null) {
            return true
        }

        val headerField = header.contentTypeField ?: return true
        val headerFieldValue = headerField.headerFieldValue ?: return true

        return SimpleTypeEnum.isTextContentType(headerFieldValue.text)
    }

    private fun handleOrdinaryContent(
        requestMessagesGroup: HttpRequestMessagesGroup?,
        variableResolver: VariableResolver,
        header: HttpHeader?,
        contentType: ContentType?,
        paramMap: Map<String, String>,
    ): Any? {
        requestMessagesGroup ?: return null

        val shouldEncode = contentType == ContentType.APPLICATION_FORM_URLENCODED
                && paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)

        var reqStr: String? = null

        val messageBody = requestMessagesGroup.messageBody
        if (messageBody != null) {
            reqStr = variableResolver.resolve(messageBody.text)

            if (shouldEncode) {
                reqStr = encodeQueryParam(reqStr)
            }
        }

        val filePath = requestMessagesGroup.inputFile?.filePath?.text ?: return reqStr

        val path = constructFilePath(filePath, variableResolver.httpFileParentPath)

        val file = File(path)

        if (isTxtContentType(header)) {
            if (reqStr == null) {
                reqStr = ""
            } else {
                reqStr += CR_LF
            }

            var str = VirtualFileUtils.readNewestContent(file)

            if (shouldEncode) {
                str = encodeQueryParam(str)
            }

            reqStr += variableResolver.resolve(str)

            return reqStr
        } else {
            val byteArray = VirtualFileUtils.readNewestBytes(file)

            val size = Formats.formatFileSize(byteArray.size.toLong())

            val desc = NlsBundle.nls("binary.body.desc", size, file.absolutePath)

            return Pair(byteArray, desc)
        }
    }

    private fun constructMultipartBody(
        boundary: String,
        httpMultipartMessage: HttpMultipartMessage,
        variableResolver: VariableResolver,
        paramMap: Map<String, String>,
    ): MutableList<Pair<ByteArray, String>> {
        val byteArrays = mutableListOf<Pair<ByteArray, String>>()

        httpMultipartMessage.multipartFieldList
            .forEach {
                val lineBoundary = "--$boundary$CR_LF"
                byteArrays.add(Pair(lineBoundary.toByteArray(), lineBoundary))

                val header = it.header

                header.headerFieldList
                    .forEach { innerIt ->
                        val headerName = innerIt.headerFieldName.text
                        val headerValue = innerIt.headerFieldValue?.text

                        val value = if (headerValue.isNullOrEmpty()) {
                            ""
                        } else {
                            variableResolver.resolve(headerValue)
                        }

                        val headerLine = "$headerName: $value$CR_LF"
                        byteArrays.add(Pair(headerLine.toByteArray(StandardCharsets.UTF_8), headerLine))
                    }

                byteArrays.add(Pair(CR_LF.toByteArray(StandardCharsets.UTF_8), CR_LF))

                val content = handleOrdinaryContent(
                    it.requestMessagesGroup,
                    variableResolver,
                    it.header,
                    it.contentType,
                    paramMap
                )

                if (content is String) {
                    val tmpContent = content + CR_LF

                    byteArrays.add(Pair(tmpContent.toByteArray(StandardCharsets.UTF_8), tmpContent))
                } else if (content is Pair<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val pair = content as Pair<ByteArray, String>

                    val bytes = pair.first
                    val desc = pair.second

                    byteArrays.add(Pair(bytes + CR_LF.toByteArray(StandardCharsets.UTF_8), desc + CR_LF))
                }
            }

        val endBoundary = "--$boundary--"
        byteArrays.add(Pair(endBoundary.toByteArray(StandardCharsets.UTF_8), endBoundary))

        return byteArrays
    }

    fun handleOrdinaryContentCurl(
        requestMessagesGroup: HttpRequestMessagesGroup,
        variableResolver: VariableResolver,
        header: HttpHeader?,
        raw: Boolean,
    ): String {
        var reqStr = ""

        val messageBody = requestMessagesGroup.messageBody
        if (messageBody != null) {
            reqStr = variableResolver.resolve(messageBody.text)
        }

        val filePath = requestMessagesGroup.inputFile?.filePath?.text
            ?: return if (raw) {
                reqStr + CR_LF
            } else {
                reqStr.replace("\n", "\n    ").replace("'", "'\\''")
            }

        val path = constructFilePath(filePath, variableResolver.httpFileParentPath)

        val file = File(path)

        if (!isTxtContentType(header)) {
            return ""
        }

        reqStr += CR_LF

        val str = VirtualFileUtils.readNewestContent(file)

        reqStr += variableResolver.resolve(str)

        return if (raw) {
            reqStr + CR_LF
        } else {
            reqStr.replace("\n", "\n    ").replace("'", "'\\''")
        }
    }

    fun constructMultipartBodyCurl(
        httpMultipartMessage: HttpMultipartMessage,
        variableResolver: VariableResolver,
        boundary: String,
        raw: Boolean,
    ): MutableList<String> {
        val list = mutableListOf<String>()

        httpMultipartMessage.multipartFieldList
            .forEach {
                val requestMessagesGroup = it.requestMessagesGroup
                val header = it.header

                if (raw) {
                    list.add("--$boundary$CR_LF")

                    header.headerFieldList.forEach { innerIt ->
                        list.add("${innerIt.name}: ${innerIt.value}$CR_LF")
                    }

                    list.add(CR_LF)
                }

                val messageBody = requestMessagesGroup.messageBody
                if (messageBody != null) {
                    val content = variableResolver.resolve(messageBody.text)

                    list.add(
                        if (raw) {
                            content + CR_LF
                        } else {
                            "    -F \"${header.contentDispositionName}=" + content + ";type=${header.contentTypeField?.headerFieldValue?.text}\""
                        }
                    )
                }

                val filePath = requestMessagesGroup.inputFile?.filePath?.text
                if (filePath != null) {
                    val path = constructFilePath(filePath, variableResolver.httpFileParentPath)

                    val file = File(path)

                    val content = "@" + file.absolutePath.replace("\\", "/")

                    list.add(
                        if (raw) {
                            "< " + file.absolutePath + CR_LF
                        } else {
                            "    -F \"${header.contentDispositionName}=" + content + ";filename=${header.contentDispositionFileName};type=${header.contentTypeField?.headerFieldValue?.text}\""
                        }
                    )
                }
            }

        if (raw) {
            list.add("--$boundary--")
        }

        return list
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
                    headerDescList.add("$t: $it$CR_LF")
                }
            }

        headerDescList.add(CR_LF)

        return headerDescList
    }

    fun convertResponse(response: HttpResponse<ByteArray>): HttpResInfo {
        val resBody = response.body()
        val resHeaders = response.headers()
        val contentType = resHeaders.firstValue(CONTENT_TYPE).getOrElse { ContentType.TEXT_PLAIN.mimeType }

        val simpleTypeEnum = SimpleTypeEnum.convertContentType(contentType)

        val bodyStr = if (simpleTypeEnum.binary) {
            null
        } else {
            val str = String(resBody, StandardCharsets.UTF_8)

            if (simpleTypeEnum == SimpleTypeEnum.JSON) {
                if (resBody.size > RES_SIZE_LIMIT) {
                    str
                } else {
                    val prettyStr = formatJson(str)
                    if (prettyStr.length> RES_SIZE_LIMIT) {
                        str
                    } else {
                        prettyStr
                    }
                }
            } else {
                str
            }
        }

        return HttpResInfo(simpleTypeEnum, resBody, bodyStr, contentType)
    }

    private fun formatJson(jsonStr: String): String {
        try {
            val jsonElement = gson.fromJson(jsonStr, JsonElement::class.java)

            return gson.toJson(jsonElement)
        } catch (_: JsonSyntaxException) {
            return jsonStr
        }
    }

    fun getJsScript(httpResponseHandler: HttpResponseHandler?): HttpScriptBody? {
        if (httpResponseHandler == null) {
            return null
        }

        return httpResponseHandler.responseScript.scriptBody
    }

    fun resolveFileGlobalVariable(variableName: String, httpFile: PsiFile): PsiElement? {
        val globalVariables = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable::class.java)

        return globalVariables
            .mapNotNull {
                val globalVariableName = it.globalVariableName
                if (globalVariableName.name == variableName) {
                    return@mapNotNull globalVariableName
                } else {
                    return@mapNotNull null
                }
            }
            .firstOrNull()
    }

    fun getPreJsFiles(httpFile: HttpFile, excludeRequire: Boolean): List<PreJsFile> {
        val directionComments = httpFile.getDirectionComments()

        val parentPath = httpFile.virtualFile.parent.path

        return directionComments
            .mapNotNull {
                val isRequire = it.directionName?.text == ParamEnum.REQUIRE.param

                if (isRequire) {
                    if (excludeRequire) {
                        return@mapNotNull null
                    } else {
                        val url = it.directionValue?.text ?: return@mapNotNull null
                        return@mapNotNull PreJsFile(it, URL(url))
                    }
                }

                val path = getDirectionPath(it, parentPath) ?: return@mapNotNull null

                val preJsFile = PreJsFile(it, null)
                preJsFile.file = File(path)

                preJsFile
            }
    }

    private fun resolveDirOfVariable(variable: HttpVariable?): PsiDirectory? {
        val references = variable?.variableName?.references ?: return null
        if (references.isEmpty()) {
            return null
        }

        val psiElement = references[0].resolve()
        if (psiElement !is PsiDirectory) {
            return null
        }

        return psiElement
    }

    fun resolvePathOfVariable(variable: HttpVariable?): String? {
        val psiElement = resolveDirOfVariable(variable) ?: return null

        return psiElement.virtualFile.path
    }

    fun getDirectionPath(directionComment: HttpDirectionComment, parentPath: String): String? {
        val directionValue = directionComment.directionValue
        if (directionValue == null || !ParamEnum.isFilePathParam(directionComment.directionName?.text)) {
            return null
        }

        var path = ""

        val resolvedPath = resolvePathOfVariable(directionValue.variable)
        if (resolvedPath != null) {
            path += resolvedPath
        }

        path += directionValue.directionValueContent?.text ?: ""

        if (!path.endsWith("js", ignoreCase = true)) {
            return null
        }

        return constructFilePath(path, parentPath)
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

    fun getReqDirectionCommentParamMap(httpRequestBlock: HttpRequestBlock): Map<String, String> {
        val map = mutableMapOf<String, String>()

        httpRequestBlock.directionCommentList
            .forEach {
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

        return getOriginalFile(requestTarget.project, tabName)
    }

    fun getOriginalFile(project: Project, tabName: String): VirtualFile? {
        val runManager = RunManager.getInstance(project)
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
            val contextPathTrim = envFileService.getEnvValue("contextPathTrim", selectedEnv, httpFileParentPath)

            val tmpIdx: Int
            val uri: URI
            try {
                uri = URI(url)
                tmpIdx = if (contextPath != null) {
                    url.indexOf(contextPath)
                } else if (contextPathTrim != null) {
                    url.indexOf(contextPathTrim) + contextPathTrim.length
                } else {
                    url.indexOf(uri.path)
                }
            } catch (_: Exception) {
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

    fun isHistoryFile(virtualFile: VirtualFile?): Boolean {
        return virtualFile?.nameWithoutExtension?.endsWith("history") == true
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
        val virtualFile = VfsUtil.findFileByIoFile(file, false) ?: return null

        if (virtualFile.isDirectory) {
            return PsiManager.getInstance(project).findDirectory(virtualFile)!!
        }

        return PsiUtil.getPsiFile(project, virtualFile)
    }

    fun collectJsonPropertyNameLevels(jsonString: JsonStringLiteral): LinkedList<String> {
        val beanFieldLevels = LinkedList<String>()

        var jsonProperty = PsiTreeUtil.getParentOfType(jsonString, JsonProperty::class.java)
        while (jsonProperty != null) {
            val propertyName = getJsonPropertyName(jsonProperty)
            beanFieldLevels.push(propertyName)
            jsonProperty = PsiTreeUtil.getParentOfType(jsonProperty, JsonProperty::class.java)
        }

        return beanFieldLevels
    }

    private fun getJsonPropertyName(jsonProperty: JsonProperty): String {
        val nameElement = jsonProperty.nameElement
        val name = nameElement.text
        return name.substring(1, name.length - 1)
    }

    fun resolveTargetParam(psiMethod: PsiMethod): PsiParameter? {
        val superPsiMethods = psiMethod.findSuperMethods(false)
        val psiParameters = psiMethod.parameterList.parameters
        var psiParameter: PsiParameter? = null

        for ((index, psiParam) in psiParameters.withIndex()) {
            var hasAnno = psiParam.hasAnnotation(REQUEST_BODY_ANNO_NAME)
            if (hasAnno) {
                psiParameter = psiParam
                break
            }

            for (superPsiMethod in superPsiMethods) {
                val superPsiParam = superPsiMethod.parameterList.parameters[index]
                hasAnno = superPsiParam.hasAnnotation(REQUEST_BODY_ANNO_NAME)
                if (hasAnno) {
                    psiParameter = psiParam
                    break
                }
            }
        }

        return psiParameter
    }

    fun resolveTargetField(
        paramPsiCls: PsiClass,
        jsonPropertyNameLevels: LinkedList<String>,
        classGenericParameters: Array<PsiType>,
    ): PsiField? {
        var psiField: PsiField? = null

        try {
            var fieldTypeCls: PsiClass
            var propertyName = jsonPropertyNameLevels.pop()

            val isCollection = InheritanceUtil.isInheritor(paramPsiCls, "java.util.Collection")
            if (isCollection) {
                if (classGenericParameters.isEmpty()) {
                    return null
                }

                // Get the generic parameter type
                fieldTypeCls = PsiUtils.resolvePsiType(classGenericParameters[0]) ?: return null
            } else {
                fieldTypeCls = paramPsiCls
            }


            while (true) {
                psiField = fieldTypeCls.findFieldByName(propertyName, true) ?: return null
                if (psiField.type !is PsiClassType) {
                    return psiField
                }

                val psiType = psiField.type as PsiClassType

                val parameters = psiType.parameters
                if (parameters.isNotEmpty()) {
                    // Get the generic parameter type
                    fieldTypeCls = PsiUtils.resolvePsiType(parameters[0]) ?: return null
                } else {
                    val psiFieldTypeCls = PsiUtils.resolvePsiType(psiType) ?: return null
                    if (psiFieldTypeCls is PsiTypeParameter && classGenericParameters.isNotEmpty()) {
                        // The parameter itself is a generic type, such as T, and the first one is taken directly
                        val genericActualType = classGenericParameters[0] as PsiClassType
                        if (genericActualType.parameters.isNotEmpty()) {
                            val psiFieldGenericTypeCls =
                                PsiUtils.resolvePsiType(genericActualType.parameters[0]) ?: return null
                            fieldTypeCls = psiFieldGenericTypeCls
                        } else {
                            fieldTypeCls = PsiUtils.resolvePsiType(genericActualType) ?: return null
                        }
                    } else {
                        fieldTypeCls = psiFieldTypeCls
                    }
                }

                propertyName = jsonPropertyNameLevels.pop()
            }
        } catch (_: NoSuchElementException) {
        }

        return psiField
    }

    fun generateAnno(annotation: PsiAnnotation): String {
        val html = """
            <div class='definition'>
                <span style="color:#808000;">@</span><a href="psi_element://${annotation.qualifiedName}"><span style="color:#808000;">${annotation.nameReferenceElement?.text}</span></a><span>${annotation.parameterList.text}</span>
            </div>
        """.trimIndent()

        return html
    }

    fun getMethodDesc(psiMethod: PsiMethod): String {
        val list = mutableListOf<String>()

        val docComment = psiMethod.docComment
        if (docComment != null) {
            val comment = getNextSiblingByType(docComment.firstChild, JavaDocTokenType.DOC_COMMENT_DATA, false)
                ?.text?.trim()

            comment?.let { list.add(it) }
        }

        val annotation = psiMethod.getAnnotation(API_OPERATION_ANNO_NAME)
        if (annotation != null) {
            val attributeValue = annotation.findAttributeValue("value") as PsiLiteralExpression?

            val desc = attributeValue?.value?.toString()?.trim()

            desc?.let { list.add(it) }
        }

        return list.joinToString(" ")
    }

    fun convertToJsString(str: String): String {
        return "`" + str.replace("\\", "\\\\").replace("`", "\\`") + "`"
    }

    fun convertReqBody(reqBody: Any?): Any {
        if (reqBody == null) {
            return "null"
        }

        if (reqBody is String) {
            return convertToJsString(reqBody)
        }

        return when (reqBody) {
            is Pair<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val pair = reqBody as Pair<ByteArray, String>

                pair.first
            }

            is MutableList<*> -> {
                @Suppress("UNCHECKED_CAST")
                val list = reqBody as MutableList<Pair<ByteArray, String>>

                list.map { it.first }.reduce { a, b -> a + b }
            }

            else -> {
                throw IllegalArgumentException(NlsBundle.nls("reqBody.unknown", reqBody.javaClass))
            }
        }

    }

    fun convertToReqBodyPublisher(reqBody: Any?): Pair<java.net.http.HttpRequest.BodyPublisher, Long> {
        if (reqBody == null) {
            return Pair(BodyPublishers.noBody(), 0L)
        }

        var multipartLength = 0L
        val bodyPublisher: java.net.http.HttpRequest.BodyPublisher

        when (reqBody) {
            is String -> {
                bodyPublisher = BodyPublishers.ofString(reqBody)
            }

            is Pair<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val pair = reqBody as Pair<ByteArray, String>

                bodyPublisher = BodyPublishers.ofByteArray(pair.first)
            }

            is List<*> -> {
                @Suppress("UNCHECKED_CAST")
                val list = reqBody as MutableList<Pair<ByteArray, String>>

                val byteArrays = list.map { it.first }

                bodyPublisher = BodyPublishers.ofByteArrays(byteArrays)

                multipartLength = byteArrays.sumOf { it.size.toLong() }
            }

            else -> {
                System.err.println(NlsBundle.nls("reqBody.unknown", reqBody.javaClass))

                bodyPublisher = BodyPublishers.noBody()
            }
        }

        return Pair(bodyPublisher, multipartLength)
    }

    fun getReqBodyDesc(reqBody: Any?): MutableList<String> {
        val maxSizeLimit = 50000
        val descList = mutableListOf<String>()

        when (reqBody) {
            is String -> {
                if (reqBody.length > maxSizeLimit) {
                    descList.add(
                        reqBody.substring(0, maxSizeLimit) + "$CR_LF......(${NlsBundle.nls("content.truncated")})"
                    )
                } else {
                    descList.add(reqBody)
                }
            }

            is Pair<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val pair = reqBody as Pair<ByteArray, String>

                descList.add(pair.second)
            }

            is List<*> -> {
                @Suppress("UNCHECKED_CAST")
                val list = reqBody as MutableList<Pair<ByteArray, String>>

                list.forEach {
                    val desc = it.second

                    val bodyDesc = if (desc.length > maxSizeLimit) {
                        desc + "$CR_LF......(${NlsBundle.nls("content.truncated")})$CR_LF"
                    } else {
                        desc
                    }

                    descList.add(bodyDesc)
                }
            }
        }

        return descList
    }

    fun getVersionDesc(version: HttpClient.Version): String {
        return if (version == HttpClient.Version.HTTP_1_1) {
            "HTTP/1.1"
        } else {
            "HTTP/2"
        }
    }

    fun pickMethodIcon(method: String): Icon {
        try {
            val methodType = HttpRequestEnum.getInstance(method)

            return methodType.icon
        } catch (ignored: UnsupportedOperationException) {
            return HttpIcons.FILE
        }
    }

    fun createGlobalVariableAndInsert(variableName: String, variableValue: String, project: Project): PsiElement? {
        val textEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null
        val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile

        val newGlobalVariable = createGlobalVariable(variableName, variableValue, project)

        val directionComments = httpFile.getDirectionComments()
        val globalHandler = httpFile.getGlobalHandler()

        val elementCopy = if (directionComments.isNotEmpty()) {
            httpFile.addAfter(newGlobalVariable, directionComments.last().nextSibling)
        } else if (globalHandler != null) {
            httpFile.addAfter(newGlobalVariable, globalHandler)
        } else {
            httpFile.addBefore(newGlobalVariable, httpFile.firstChild)
        }

        val whitespace = newGlobalVariable.nextSibling
        elementCopy.add(whitespace)

        val cr = whitespace.nextSibling
        if (cr != null) {
            elementCopy.add(cr)
        }

        return elementCopy
    }

    fun modifyFileGlobalVariable(
        key: String,
        newKey: String,
        newValue: String,
        add: Boolean,
        project: Project,
    ): Boolean {
        return WriteCommandAction.runWriteCommandAction(project, Computable {
            if (add) {
                val variable = createGlobalVariableAndInsert(newKey, newValue, project)

                variable != null
            } else {
                val textEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@Computable false

                val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile
                val children = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariable::class.java)

                val globalVariable = children
                    .firstOrNull { it: HttpGlobalVariable -> it.globalVariableName.name == key }
                    ?: return@Computable false

                if (key != newKey) {
                    val renameProcessor = RenameProcessor(
                        project, globalVariable, newKey,
                        GlobalSearchScope.projectScope(project), false, true
                    )
                    renameProcessor.run()
                }

                val newGlobalVariable = createGlobalVariable(newKey, newValue, project)

                globalVariable.replace(newGlobalVariable)
            }

            true
        })
    }

    fun modifyJsVariable(newKey: String, newValue: String) {
        setGlobalVariable(newKey, newValue)
    }

    fun modifyEnvVariable(
        key: String,
        newKey: String,
        newValue: String,
        add: Boolean,
        project: Project,
    ): Boolean {
        val triple = HttpEditorTopForm.getTriple(project) ?: return false

        val selectedEnv = triple.first
        val httpFileParentPath = triple.second.parent.path

        if (add) {
            val jsonProperty = getEnvJsonProperty(selectedEnv, httpFileParentPath, project) ?: return false

            val jsonValue = jsonProperty.value as? JsonObject ?: return false

            WriteCommandAction.runWriteCommandAction(project) {
                val newProperty = createStringProperty(project, newKey, newValue)
                val newComma = getNextSiblingByType(newProperty, JsonElementTypes.COMMA, false)
                val propertyList = jsonValue.propertyList

                if (propertyList.isEmpty()) {
                    jsonValue.addAfter(newProperty, jsonValue.firstChild)
                } else {
                    val psiElement = jsonValue.addAfter(newComma!!, propertyList[propertyList.size - 1])
                    jsonValue.addAfter(newProperty, psiElement)
                }
            }
        } else {
            val jsonLiteral = getEnvEleLiteral(key, selectedEnv, httpFileParentPath, project) ?: return false

            val jsonProperty = jsonLiteral.parent

            if (key != newKey) {
                val renameProcessor = RenameProcessor(
                    project, jsonProperty, newKey,
                    GlobalSearchScope.projectScope(project), false, true
                )
                renameProcessor.run()
            }

            WriteCommandAction.runWriteCommandAction(project) {
                val newProperty = when (jsonLiteral) {
                    is JsonNumberLiteral -> {
                        createNumberProperty(project, newKey, newValue)
                    }

                    is JsonBooleanLiteral -> {
                        createBoolProperty(project, newKey, newValue)
                    }

                    else -> {
                        createStringProperty(project, newKey, newValue)
                    }
                }

                jsonProperty.replace(newProperty)
            }
        }

        return true
    }

    fun getActiveValidProject(): Project? {
        val project = ProjectUtil.getActiveProject() ?: return null
        if (!project.isInitialized) {
            return null
        }

        if (project.isDisposed) {
            return null
        }

        return project
    }

}
