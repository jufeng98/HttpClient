package org.javamaster.httpclient.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.adapter.DateTypeAdapter
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.psi.HttpPsiUtils.getNextSiblingByType
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.runconfig.HttpConfigurationType
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.ui.HttpEditorTopForm
import java.io.File
import java.net.URI
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*
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

    const val READ_TIMEOUT = 3600L
    const val CONNECT_TIMEOUT = 30L
    const val TIMEOUT = 10_000

    const val HTTP_TYPE_ID = "intellijHttpClient"
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

    fun convertToReqBody(request: HttpRequest, variableResolver: VariableResolver): Any? {
        if (request.contentLength != null) {
            throw IllegalArgumentException("Can't have Content-Length header!")
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
                request.contentTypeBoundary ?: throw IllegalArgumentException("$CONTENT_TYPE header missing boundary!")
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
                    return@map psiElement.parent
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

    fun getPreJsFiles(httpFile: HttpFile): List<PreJsFile> {
        val directionComments = httpFile.getDirectionComments()

        val project = httpFile.project
        val parentPath = httpFile.virtualFile.parent.path

        return directionComments
            .mapNotNull {
                val path = getDirectionPath(it, parentPath, project) ?: return@mapNotNull null

                PreJsFile(it, File(path))
            }
    }

    fun getDirectionPath(directionComment: HttpDirectionComment, parentPath: String, project: Project): String? {
        if (directionComment.directionValue == null || directionComment.directionName?.text != ParamEnum.IMPORT.param) {
            return null
        }

        var path = directionComment.directionValue!!.text
        if (path.length < 3) {
            return null
        }

        path = path.substring(1, path.length - 1)
        if (!path.endsWith("js", ignoreCase = true)) {
            return null
        }

        path = VariableResolver.resolveInnerVariable(path, parentPath, project)

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
        val list = ArrayList<String>(2)

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
}
