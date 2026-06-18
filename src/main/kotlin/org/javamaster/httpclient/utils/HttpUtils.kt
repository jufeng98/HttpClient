package org.javamaster.httpclient.utils

import com.intellij.execution.RunManager
import com.intellij.ide.impl.ProjectUtil
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.consts.HttpConsts.Companion.VAR_BRACE_END
import org.javamaster.httpclient.consts.HttpConsts.Companion.VAR_BRACE_START
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.exception.HeaderUnresolvedVariableException
import org.javamaster.httpclient.exception.HttpFileException
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.service.HistoryFolderService
import org.javamaster.httpclient.utils.ReqUtils.Companion.encodeQueryParam
import org.javamaster.httpclient.utils.ReqUtils.Companion.removeQueryParamSpaceAndCr
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.name

/**
 * @author yudong
 */
object HttpUtils {
    const val CR_LF = "\r\n"

    fun getTabName(method: HttpMethod): String {
        val requestBlock = computeReadAction { PsiTreeUtil.getParentOfType(method, HttpRequestBlock::class.java)!! }

        return getTabName(requestBlock)
    }

    fun getTabName(requestBlock: HttpRequestBlock): String {
        val comment = requestBlock.comment
        if (comment != null) {
            val text = comment.text
            val tabName = text.substring(3).trim()
            if (tabName.isNotEmpty()) {
                return tabName
            }
        }

        val httpFile = computeReadAction { requestBlock.parent as HttpFile }
        val requestBlocks = httpFile.getRequestBlocks()

        for ((index, httpRequestBlock) in requestBlocks.withIndex()) {
            if (requestBlock == httpRequestBlock) {
                return "HTTP Request ▏#${index + 1}"
            }
        }

        return "HTTP Request ▏#0"
    }

    fun getFilePathText(filePath: HttpFilePath?): String {
        if (filePath == null) {
            return ""
        }

        val text = filePath.filePathContentList.lastOrNull()?.text
        if (filePath.variableList.isEmpty()) {
            if (text != null && text.length > 32) {
                return Paths.get(text).name
            }
        }

        return text ?: "..."
    }

    fun convertToReqHeaderMap(
        headerFields: List<HttpHeaderField>?,
        variableResolver: VariableResolver,
    ): LinkedMultiValueMap<String, String?> {
        if (headerFields.isNullOrEmpty()) return LinkedMultiValueMap()

        val map = LinkedMultiValueMap<String, String?>()

        runReadAction {
            headerFields.stream()
                .forEach {
                    val headerName = it.headerFieldName.text
                    val headerValue = it.headerFieldValue?.text ?: ""
                    map.add(headerName, variableResolver.resolve(headerValue))
                }
        }

        return map
    }

    fun resolveReqHeaderMapAgain(
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
        variableResolver: VariableResolver,
    ): LinkedMultiValueMap<String, String?> {
        val map = LinkedMultiValueMap<String, String?>()

        reqHeaderMap.entries.stream()
            .forEach {
                val headerName = it.key
                val values = it.value

                values.forEach { value ->
                    val resolved = if (value == null) {
                        null
                    } else {
                        val content = variableResolver.resolve(value)

                        val idxStart = content.indexOf(VAR_BRACE_START)
                        if (idxStart != -1) {
                            val idxEnd = content.indexOf(VAR_BRACE_END, idxStart)
                            if (idxEnd != -1) {
                                throw HeaderUnresolvedVariableException(
                                    content.substring(idxStart + VAR_BRACE_START.length, idxEnd)
                                )
                            }
                        }

                        content
                    }

                    map.add(headerName, resolved)
                }
            }

        return map
    }

    fun convertToReqBody(
        request: HttpRequest,
        variableResolver: VariableResolver,
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
                request.contentType
            )
        }

        val httpMultipartMessage = body?.multipartMessage
        if (httpMultipartMessage != null) {
            val boundary = request.contentTypeBoundary
                ?: throw IllegalArgumentException(NlsBundle.nls("lack.boundary", CONTENT_TYPE))

            return constructMultipartBody(boundary, httpMultipartMessage, variableResolver)
        }

        return null
    }

    fun isTxtContentType(contentType: ContentType?): Boolean {
        if (contentType == null) {
            return false
        }

        return SimpleTypeEnum.isTextContentType(contentType.mimeType)
    }

    private fun isTxtContentType(header: HttpHeader?): Boolean {
        if (header == null) {
            return true
        }

        val headerField = header.contentTypeField ?: return true
        val headerFieldValue = headerField.headerFieldValue ?: return true

        return SimpleTypeEnum.isTextContentType(headerFieldValue.text)
    }

    fun <T> computeReadAction(runnable: () -> T): T {
        return ApplicationManager.getApplication().runReadAction(Computable(runnable))
    }

    fun runReadAction(runnable: () -> Unit) {
        ApplicationManager.getApplication().runReadAction(Computable(runnable))
    }

    private fun handleOrdinaryContent(
        requestMessagesGroup: HttpRequestMessagesGroup,
        variableResolver: VariableResolver,
        header: HttpHeader?,
        contentType: ContentType?,
    ): Triple<ByteArray?, String?, ContentType?>? {
        val formUrlEncodeReq = contentType?.mimeType == ContentType.APPLICATION_FORM_URLENCODED.mimeType

        var reqStr: String? = null

        val messageBody = requestMessagesGroup.messageBody
        val formUrlencodedBody = requestMessagesGroup.formUrlencodedBody

        if (messageBody != null) {
            reqStr = computeReadAction { variableResolver.resolve(messageBody.text) }
        } else if (formUrlencodedBody != null) {
            reqStr = computeReadAction { variableResolver.resolve(formUrlencodedBody.text) }

            if (formUrlEncodeReq) {
                reqStr = removeQueryParamSpaceAndCr(reqStr)
            }
        }

        val filePath = requestMessagesGroup.inputFile?.filePath
        if (filePath == null) {
            val charset = contentType?.charset ?: StandardCharsets.UTF_8
            return Triple(reqStr?.toByteArray(charset), reqStr, contentType)
        }

        var filePathStr = variableResolver.resolve(filePath.text)

        val path = constructFilePath(filePathStr, variableResolver.httpFileParentPath)

        val file = File(path)

        if (isTxtContentType(header)) {
            if (reqStr == null) {
                reqStr = ""
            } else {
                reqStr += CR_LF
            }

            var str = VirtualFileUtils.readNewestContent(file)

            if (formUrlEncodeReq) {
                str = removeQueryParamSpaceAndCr(str)
            }

            reqStr += computeReadAction { variableResolver.resolve(str) }

            val charset = contentType?.charset ?: StandardCharsets.UTF_8
            return Triple(reqStr.toByteArray(charset), reqStr, contentType)
        } else {
            val byteArray = VirtualFileUtils.readNewestBytes(file)

            val size = Formats.formatFileSize(byteArray.size.toLong())

            val desc = NlsBundle.nls("binary.body.desc", size, file.absolutePath)

            return Triple(byteArray, desc, contentType)
        }
    }

    private fun constructMultipartBody(
        boundary: String,
        httpMultipartMessage: HttpMultipartMessage,
        variableResolver: VariableResolver,
    ): MutableList<Triple<ByteArray?, String?, ContentType?>> {
        val list = mutableListOf<Triple<ByteArray?, String?, ContentType?>>()

        httpMultipartMessage.multipartFieldList
            .forEach {
                val lineBoundary = "--$boundary$CR_LF"
                list.add(Triple(lineBoundary.toByteArray(), lineBoundary, null))

                val header = it.header

                runReadAction {
                    header.headerFieldList
                        .forEach { innerIt ->
                            val headerName = innerIt.headerFieldName.text
                            val headerValue = innerIt.headerFieldValue?.text

                            val value = if (headerValue.isNullOrEmpty()) "" else variableResolver.resolve(headerValue)

                            val headerLine = "$headerName: $value$CR_LF"
                            list.add(Triple(headerLine.toByteArray(StandardCharsets.UTF_8), headerLine, null))
                        }
                }

                list.add(Triple(CR_LF.toByteArray(StandardCharsets.UTF_8), CR_LF, null))

                val triple = handleOrdinaryContent(
                    it.requestMessagesGroup, variableResolver, it.header, it.contentType
                )

                if (triple != null) {
                    var first = triple.first
                    if (first != null) {
                        val charset = triple.third?.charset ?: StandardCharsets.UTF_8
                        first += CR_LF.toByteArray(charset)
                    }

                    var second = triple.second
                    if (second != null) {
                        second += CR_LF
                    }

                    list.add(Triple(first, second, triple.third))
                }
            }

        val endBoundary = "--$boundary--"
        list.add(Triple(endBoundary.toByteArray(StandardCharsets.UTF_8), endBoundary, null))

        return list
    }

    fun handleOrdinaryContentCurl(
        requestMessagesGroup: HttpRequestMessagesGroup,
        variableResolver: VariableResolver,
        request: HttpRequest,
        header: HttpHeader?,
        paramMap: Map<String, String>,
        raw: Boolean,
    ): String {
        val formUrlEncodeReq = request.contentType == ContentType.APPLICATION_FORM_URLENCODED

        val shouldEncode = formUrlEncodeReq && paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)

        val messageBody = requestMessagesGroup.messageBody
        val formUrlencodedBody = requestMessagesGroup.formUrlencodedBody

        var reqStr = ""
        if (messageBody != null) {
            reqStr = computeReadAction { variableResolver.resolve(messageBody.text) }
        } else if (formUrlencodedBody != null) {
            reqStr = computeReadAction { variableResolver.resolve(formUrlencodedBody.text) }

            if (formUrlEncodeReq) {
                reqStr = removeQueryParamSpaceAndCr(reqStr)
            }

            if (shouldEncode) {
                reqStr = encodeQueryParam(reqStr)
            }
        }

        val filePath = requestMessagesGroup.inputFile?.filePath
        if (filePath == null) {
            return if (raw) {
                reqStr + CR_LF
            } else {
                reqStr.replace("\n", "\n    ").replace("'", "'\\''")
            }
        }

        val path = constructFilePath(variableResolver.resolve(filePath.text), variableResolver.httpFileParentPath)

        if (!isTxtContentType(header)) {
            return ""
        }

        reqStr += CR_LF

        val file = File(path)

        var str = VirtualFileUtils.readNewestContent(file)

        if (formUrlEncodeReq) {
            str = removeQueryParamSpaceAndCr(str)
        }

        if (shouldEncode) {
            str += encodeQueryParam(str)
        }

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
        paramMap: Map<String, String>,
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
                val formUrlencodedBody = requestMessagesGroup.formUrlencodedBody

                val bodyEle = messageBody ?: formUrlencodedBody
                if (bodyEle != null) {
                    var content = computeReadAction { variableResolver.resolve(bodyEle.text) }

                    val formUrlEncodeReq = it.contentType?.mimeType == ContentType.APPLICATION_FORM_URLENCODED.mimeType
                    if (formUrlEncodeReq) {
                        content = removeQueryParamSpaceAndCr(content)
                    }

                    val shouldEncode = formUrlEncodeReq && paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)
                    if (shouldEncode) {
                        content = encodeQueryParam(content)
                    }

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
                    val path =
                        constructFilePath(variableResolver.resolve(filePath), variableResolver.httpFileParentPath)

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

    fun getPreJsFiles(httpFile: HttpFile, excludeRequire: Boolean, checkFile: Boolean): List<PreJsFile> {
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
                        @Suppress("DEPRECATION")
                        return@mapNotNull PreJsFile(it, URL(url))
                    }
                }

                val path = getDirectionPath(it, parentPath) ?: return@mapNotNull null

                val preJsFile = PreJsFile(it, null)
                val file = File(path)

                if (checkFile) {
                    val virtualFile = findVirtualFile(path, true)
                    if (virtualFile == null) {
                        val doc = FileDocumentManager.getInstance().getDocument(httpFile.virtualFile)!!
                        val ln = doc.getLineNumber(it.textOffset) + 1
                        val msg = NlsBundle.nls("file.not.exists", file.absolutePath) + "(${httpFile.name}#${ln})"
                        throw HttpFileException(msg)
                    }
                }

                preJsFile.file = file

                preJsFile
            }
    }

    private fun resolveVariable(variable: HttpVariable?): PsiElement? {
        val references = variable?.variableName?.references ?: return null
        if (references.isEmpty()) {
            return null
        }

        return references[0].resolve()
    }

    fun resolvePathOfVariable(variable: HttpVariable?): String? {
        val psiElement = resolveVariable(variable) ?: return null

        if (psiElement is PsiDirectory) {
            return psiElement.virtualFile.path
        }

        if (psiElement is HttpFileVariableName) {
            val fileVariable = psiElement.parent as HttpFileVariable
            return fileVariable.fileVariableValue?.text
        }

        return null
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

    fun getOriginalFile(requestTarget: HttpRequestTarget): VirtualFile? {
        val virtualFile = PsiUtil.getVirtualFile(requestTarget)

        val project = requestTarget.project
        if (isFileInHistoryDir(virtualFile, project)) {
            val httpMethod = PsiTreeUtil.getPrevSiblingOfType(requestTarget, HttpMethod::class.java) ?: return null

            val tabName = getTabName(httpMethod)

            return getOriginalFile(project, tabName)
        }

        return virtualFile
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

        return LocalFileSystem.getInstance().findFileByIoFile(File(httpRunConfiguration.httpFilePath))
    }

    fun isFileInHistoryDir(virtualFile: VirtualFile?, project: Project): Boolean {
        virtualFile ?: return false

        val ideaDirFile = project.getService(HistoryFolderService::class.java).getHistoryFolder()

        return VfsUtil.isAncestor(ideaDirFile ?: return false, virtualFile, true)
    }

    fun isRunTabName(path: String): Boolean {
        return path.startsWith("#")
    }

    fun getTargetTabName(name: String): String? {
        if (!name.startsWith("#")) {
            return null
        }

        val idx = name.indexOf(" ")
        if (idx == -1) {
            return null
        }

        return name.substring(idx).trim()
    }

    fun findVirtualFile(httpFilePath: String, refresh: Boolean = false): VirtualFile? {
        val file = File(httpFilePath)
        var virtualFile = VfsUtil.findFileByIoFile(file, refresh)
        if (virtualFile == null) {
            val url = javaClass.classLoader.getResource("examples/${file.name}")
            if (url == null) {
                return null
            }

            virtualFile = VfsUtil.findFileByURL(url)
        }

        return virtualFile
    }

    fun getTargetHttpMethod(httpFilePath: String, runConfigName: String, project: Project): HttpMethod? {
        var virtualFile = findVirtualFile(httpFilePath) ?: return null

        val httpFile = PsiUtil.getPsiFile(project, virtualFile) as HttpFile
        val httpMethods = httpFile.getHttpMethods()

        return httpMethods.firstOrNull {
            val tabName = getTabName(it)
            runConfigName == tabName
        }
    }

    fun resolveToActualFilePath(httpFilePath: HttpFilePath): String {
        var path = ""

        var child = httpFilePath.firstChild
        while (child != null) {
            if (child is HttpVariable) {
                val resolvedPath = resolvePathOfVariable(child)
                if (resolvedPath != null) {
                    path += resolvedPath
                }
            } else if (child is HttpFilePathContent) {
                val filePathContent = child
                path += filePathContent.text ?: ""
            }

            child = child.nextSibling
        }

        return path
    }

    fun resolveFilePath(path: String, httpFileParentPath: String, project: Project): PsiElement? {
        val filePath = constructFilePath(path, httpFileParentPath)

        val virtualFile = findVirtualFile(filePath) ?: return null

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
                fieldTypeCls = PsiTypeUtils.resolvePsiType(classGenericParameters[0]) ?: return null
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
                    fieldTypeCls = PsiTypeUtils.resolvePsiType(parameters[0]) ?: return null
                } else {
                    val psiFieldTypeCls = PsiTypeUtils.resolvePsiType(psiType) ?: return null
                    if (psiFieldTypeCls is PsiTypeParameter && classGenericParameters.isNotEmpty()) {
                        // The parameter itself is a generic type, such as T, and the first one is taken directly
                        val genericActualType = classGenericParameters[0] as PsiClassType
                        if (genericActualType.parameters.isNotEmpty()) {
                            val psiFieldGenericTypeCls =
                                PsiTypeUtils.resolvePsiType(genericActualType.parameters[0]) ?: return null
                            fieldTypeCls = psiFieldGenericTypeCls
                        } else {
                            fieldTypeCls = PsiTypeUtils.resolvePsiType(genericActualType) ?: return null
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

    fun getContentType(contentTypeHeader: String): ContentType {
        val split = contentTypeHeader.split(";")

        val mimeType = split[0]
        if (split.size == 1) {
            return getByMimeType(mimeType)
        }

        val strList = split[1].split("=")
        if (strList.size != 2) {
            return ContentType.getByMimeType(mimeType)
        }

        if (!strList[0].trim().equals("charset", true)) {
            return ContentType.getByMimeType(mimeType)
        }

        return ContentType.getByMimeType(mimeType).withCharset(strList[1].trim())
    }

    private fun getByMimeType(mimeType: String): ContentType {
        val contentType = ContentType.getByMimeType(mimeType)
        if (contentType != null) {
            return contentType
        }

        return ContentType.create(mimeType)
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
