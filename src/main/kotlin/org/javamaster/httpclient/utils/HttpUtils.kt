package org.javamaster.httpclient.utils

import com.intellij.execution.RunManager
import com.intellij.ide.impl.ProjectUtil
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
import org.javamaster.httpclient.utils.ReqUtils.Companion.encodeQueryParam
import org.javamaster.httpclient.utils.ReqUtils.Companion.handleQueryParam
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author yudong
 */
object HttpUtils {
    const val CR_LF = "\r\n"

    fun getTabName(httpMethod: HttpMethod): String {
        val requestBlock = PsiTreeUtil.getParentOfType(httpMethod, HttpRequestBlock::class.java)!!
        val comment = requestBlock.comment
        if (comment != null) {
            val text = comment.text
            val tabName = text.substring(3).trim()
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

        val formUrlEncodeReq = contentType == ContentType.APPLICATION_FORM_URLENCODED
        val shouldEncode = formUrlEncodeReq && paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)

        var reqStr: String? = null

        val messageBody = requestMessagesGroup.messageBody
        if (messageBody != null) {
            reqStr = variableResolver.resolve(messageBody.text)

            if (formUrlEncodeReq) {
                reqStr = handleQueryParam(reqStr)
            }

            if (shouldEncode) {
                reqStr = encodeQueryParam(reqStr)
            }
        }

        val filePath = requestMessagesGroup.inputFile?.filePath ?: return reqStr

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

        val path = constructFilePath(variableResolver.resolve(filePath), variableResolver.httpFileParentPath)

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
                        @Suppress("DEPRECATION")
                        return@mapNotNull PreJsFile(it, URL(url))
                    }
                }

                val path = getDirectionPath(it, parentPath) ?: return@mapNotNull null

                val preJsFile = PreJsFile(it, null)
                preJsFile.file = File(path)

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

        if (psiElement is HttpGlobalVariableName) {
            val globalVariable = psiElement.parent as HttpGlobalVariable
            return globalVariable.globalVariableValue?.text
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

    fun isFileInIdeaDir(virtualFile: VirtualFile?): Boolean {
        return virtualFile?.name?.startsWith("tmp") == true
    }

    fun isHistoryFile(virtualFile: VirtualFile?): Boolean {
        return virtualFile?.nameWithoutExtension?.endsWith("history") == true
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

    fun getTargetHttpMethod(httpFilePath: String, runConfigName: String, project: Project): HttpMethod? {
        val virtualFile = VfsUtil.findFileByIoFile(File(httpFilePath), false) ?: return null

        val psiFile = PsiUtil.getPsiFile(project, virtualFile)
        val httpMethods = PsiTreeUtil.findChildrenOfType(psiFile, HttpMethod::class.java)

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
            } else {
                val filePathContent = child as HttpFilePathContent
                path += filePathContent.text ?: ""
            }

            child = child.nextSibling
        }

        return path
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
