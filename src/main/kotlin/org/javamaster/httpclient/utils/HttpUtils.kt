package org.javamaster.httpclient.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.intellij.execution.RunManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.util.elementType
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.runconfig.HttpConfigurationType
import org.javamaster.httpclient.runconfig.HttpRunConfiguration
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
        .create()

    fun saveConfiguration(tabName: String, project: Project, selectedEnv: String?, httpMethod: HttpMethod) {
        val runManager = RunManager.getInstance(project)

        var config = runManager.findConfigurationByName(tabName)
        if (config == null) {
            config = runManager.createConfiguration(tabName, HttpConfigurationType::class.java)
            val httpRunConfiguration = config.configuration as HttpRunConfiguration
            httpRunConfiguration.env = selectedEnv ?: ""
            httpRunConfiguration.httpFilePath = httpMethod.containingFile.virtualFile.path
            runManager.addConfiguration(config)
            runManager.selectedConfiguration = config
        } else {
            val httpRunConfiguration = config.configuration as HttpRunConfiguration
            httpRunConfiguration.env = selectedEnv ?: ""
            httpRunConfiguration.httpFilePath = httpMethod.containingFile.virtualFile.path
            runManager.selectedConfiguration = config
        }
    }

    fun getTabName(httpMethod: HttpMethod): String {
        val commentTxt = getReqCommentTxt(httpMethod)
        return httpMethod.text + " " + commentTxt.replace("#", "").trim()
    }

    private fun getReqCommentTxt(httpMethod: HttpMethod): String {
        val httpRequest =
            PsiTreeUtil.getParentOfType(httpMethod, HttpRequest::class.java)!!

        val comment = PsiTreeUtil.findChildOfType(httpRequest, PsiComment::class.java)
        val blankStr = "http-client"
        if (comment != null || httpRequest.prevSibling is HttpRequest) {
            return blankStr
        }

        val psiComment = PsiTreeUtil.getPrevSiblingOfType(httpRequest, PsiComment::class.java) ?: return blankStr
        return psiComment.text
    }

    fun convertToReqHeaderMap(
        httpHeaders: HttpHeaders?,
        variableResolver: VariableResolver,
        selectedEnv: String?,
        httpFileParentPath: String,
    ): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        if (httpHeaders != null) {
            val headerList = httpHeaders.headerList
            headerList.stream()
                .forEach {
                    val text = it.text
                    val split = text.split(":")
                    if (split.size != 2) {
                        throw IllegalArgumentException("请求头${text}有错误")
                    }
                    map[split[0]] = variableResolver.resolve(split[1], selectedEnv, httpFileParentPath)
                }
        }
        return map
    }

    fun convertToReqBody(
        httpBody: HttpBody?,
        variableResolver: VariableResolver,
        selectedEnv: String?,
        httpFileParentPath: String,
    ): Any? {
        if (httpBody == null) {
            return null
        }

        val parentPath = httpBody.containingFile.virtualFile.parent.path

        val ordinaryContent = httpBody.ordinaryContent
        if (ordinaryContent != null) {
            return handleOrdinaryContent(
                ordinaryContent,
                variableResolver,
                selectedEnv,
                parentPath,
                httpFileParentPath
            )
        }

        val multipartContent = httpBody.multipartContent
        if (multipartContent != null) {
            return constructMultipartBody(
                multipartContent,
                variableResolver,
                selectedEnv,
                parentPath,
                httpFileParentPath
            )

        }

        return null
    }

    private fun handleOrdinaryContent(
        ordinaryContent: HttpOrdinaryContent,
        variableResolver: VariableResolver,
        selectedEnv: String?,
        parentPath: String,
        httpFileParentPath: String,
    ): Any? {
        val elementType = ordinaryContent.firstChild.elementType
        if (elementType == HttpTypes.JSON_TEXT || elementType == HttpTypes.XML_TEXT
            || elementType == HttpTypes.URL_FORM_ENCODE || elementType == HttpTypes.URL_DESC
        ) {
            return variableResolver.resolve(ordinaryContent.text, selectedEnv, httpFileParentPath)
        }

        val httpFile = ordinaryContent.file ?: return null
        val filePath = httpFile.filePath.text

        val path = constructFilePath(filePath, parentPath)
        val virtualFile = VfsUtil.findFileByIoFile(File(path), true)
            ?: throw IllegalArgumentException("文件:${path}不存在")

        if (filePath.endsWith("json") || filePath.endsWith("xml")
            || filePath.endsWith("txt") || filePath.endsWith("text")
        ) {
            val str = VfsUtil.loadText(virtualFile)
            return variableResolver.resolve(str, selectedEnv, httpFileParentPath)
        }

        return VfsUtil.loadBytes(virtualFile)
    }

    private fun constructMultipartBody(
        multipartContent: HttpMultipartContent,
        variableResolver: VariableResolver,
        selectedEnv: String?,
        parentPath: String,
        httpFileParentPath: String,
    ): MutableList<ByteArray> {
        val byteArrays = mutableListOf<ByteArray>()

        multipartContent.multipartBodyList.forEach {
            byteArrays.add((it.multipartSeperate.text + "\r\n").toByteArray(StandardCharsets.UTF_8))

            it.headerList.forEach { header ->
                byteArrays.add((header.text + "\r\n").toByteArray(StandardCharsets.UTF_8))
            }
            byteArrays.add("\r\n".toByteArray(StandardCharsets.UTF_8))

            val ordinaryContent = it.ordinaryContent
            val content = handleOrdinaryContent(
                ordinaryContent,
                variableResolver,
                selectedEnv,
                parentPath,
                httpFileParentPath
            )

            if (content is String) {
                byteArrays.add((content + "\r\n").toByteArray(StandardCharsets.UTF_8))
            } else if (content is ByteArray) {
                byteArrays.add(content + "\r\n".toByteArray(StandardCharsets.UTF_8))
            }
        }

        byteArrays.add((multipartContent.multipartSeperate.text).toByteArray(StandardCharsets.UTF_8))

        return byteArrays
    }

    fun constructFilePath(filePath: String, parentPath: String): String {
        return if (filePath.startsWith("/") || filePath[1] == ':') {
            // 绝对路径
            filePath
        } else {
            "$parentPath/$filePath"
        }
    }

    fun convertToResHeaderDescList(
        response: HttpResponse<ByteArray>,
    ): MutableList<String> {
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

    fun convertToResPair(response: HttpResponse<ByteArray>): Pair<String, ByteArray> {
        val resBody = response.body()
        val resHeaders = response.headers()
        val contentType = resHeaders.firstValue(org.apache.http.HttpHeaders.CONTENT_TYPE).getOrElse { "text/plain" }

        if (contentType.contains("json")) {
            val jsonStr = String(resBody, StandardCharsets.UTF_8)
            val jsonElement = gson.fromJson(jsonStr, JsonElement::class.java)
            val jsonStrPretty = gson.toJson(jsonElement)
            return Pair("json", jsonStrPretty.toByteArray(StandardCharsets.UTF_8))
        }

        if (contentType.contains("html")) {
            return Pair("html", resBody)
        }

        if (contentType.contains("xml")) {
            return Pair("xml", resBody)
        }

        if (contentType.contains("jpg") || contentType.contains("jpeg") || contentType.contains("png")) {
            return Pair("image", resBody)
        }

        return Pair("txt", resBody)
    }

    fun getJsScript(httpScript: HttpScript?): String? {
        if (httpScript == null) {
            return null
        }

        val text = httpScript.text
        return text.substring(4, text.length - 2)
    }

    fun collectBeforeJsScripts(httpFile: PsiFile): List<String> {
        val globalScripts = PsiTreeUtil.getChildrenOfType(httpFile, HttpGlobalScript::class.java) ?: arrayOf()
        return globalScripts
            .mapNotNull {
                getJsScript(it.script)
            }
    }

    fun getOriginalFile(httpUrl: HttpUrl): VirtualFile? {
        val virtualFile = PsiUtil.getVirtualFile(httpUrl)
        if (!isFileInIdeaDir(virtualFile)) {
            return virtualFile
        }

        val httpMethod = PsiTreeUtil.getPrevSiblingOfType(httpUrl, HttpMethod::class.java) ?: return null

        val tabName = if (isFileInIdeaDir(httpUrl.containingFile.virtualFile)) {
            getReqCommentTxt(httpMethod).replace("#", "").trim()
        } else {
            getTabName(httpMethod)
        }

        val project = httpUrl.project
        val runManager = RunManager.getInstance(project)
        val settings = runManager.findConfigurationByTypeAndName("gitFlowPlusHttpClient", tabName) ?: return null
        val httpRunConfiguration = settings.configuration as HttpRunConfiguration

        return VfsUtil.findFileByIoFile(File(httpRunConfiguration.httpFilePath), true)
    }

    fun getOriginalModule(httpUrl: HttpUrl): Module? {
        val project = httpUrl.project

        val virtualFile = getOriginalFile(httpUrl) ?: return null

        return ModuleUtilCore.findModuleForFile(virtualFile, project)
    }

    fun getSearchTxtInfo(httpUrl: HttpUrl, httpFileParentPath: String): Pair<String, TextRange>? {
        val project = httpUrl.project

        val url = httpUrl.text.trim()

        val start: Int
        val bracketIdx = url.indexOf("}}")
        start = if (bracketIdx != -1) {
            bracketIdx + 2
        } else {
            val envFileService = EnvFileService.getService(project)
            val contextPath = envFileService.getEnvValue("contextPath", null, httpFileParentPath)

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

        val textRange = TextRange(start, end)
        val searchTxt = url.substring(start, end)
        return Pair(searchTxt, textRange)
    }

    fun isFileInIdeaDir(virtualFile: VirtualFile?): Boolean {
        return virtualFile?.name?.startsWith("tmp") == true
    }
}
