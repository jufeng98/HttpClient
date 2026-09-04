package org.javamaster.httpclient.utils

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.HttpStatus
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts.Companion.RES_SIZE_LIMIT
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.exception.JsScriptException
import org.javamaster.httpclient.js.support.JsExecuteResult
import org.javamaster.httpclient.logger.HttpRequestLogger.logInfo
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.processHandler.HttpProcessHandler
import org.javamaster.httpclient.utils.DecompressUtils.decompressBodyBytes
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import org.javamaster.httpclient.utils.JsonUtils.formatJson
import org.javamaster.httpclient.utils.PathUtils.legalizeFileName
import org.javamaster.httpclient.utils.VirtualFileUtils.getDateHistoryDir
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpHeaders
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.jvm.optionals.getOrElse

object ResUtils {

    fun convertResponseHeaders(headers: HttpHeaders): MutableList<String> {
        val headerDescList = mutableListOf<String>()

        headers.map()
            .forEach { (t, u) ->
                u.forEach {
                    headerDescList.add("$t: $it$CR_LF")
                }
            }

        headerDescList.add(CR_LF)

        return headerDescList
    }

    fun convertResponseBody(resBody: ByteArray, resHeaders: HttpHeaders): HttpResInfo {
        var bodyBytes = resBody
        val contentEncoding = resHeaders.firstValue(org.apache.http.HttpHeaders.CONTENT_ENCODING).getOrElse { null }
        if (contentEncoding != null) {
            bodyBytes = decompressBodyBytes(bodyBytes, contentEncoding)
        }

        val contentTypeHeader = resHeaders.firstValue(CONTENT_TYPE).getOrElse { ContentType.TEXT_PLAIN.mimeType }

        val contentType = HttpUtils.getContentType(contentTypeHeader)

        val simpleTypeEnum = SimpleTypeEnum.convertContentType(contentType.mimeType)

        val bodyStr = if (simpleTypeEnum.binary) {
            null
        } else {
            val charset = contentType.charset ?: StandardCharsets.UTF_8
            val str = String(bodyBytes, charset)

            if (simpleTypeEnum == SimpleTypeEnum.JSON) {
                if (bodyBytes.size > RES_SIZE_LIMIT) {
                    str
                } else {
                    val prettyStr = formatJson(str)
                    if (prettyStr.length > RES_SIZE_LIMIT) {
                        str
                    } else {
                        bodyBytes = prettyStr.toByteArray(charset)

                        prettyStr
                    }
                }
            } else {
                str
            }
        }

        return HttpResInfo(simpleTypeEnum, bodyBytes, bodyStr, contentType.mimeType)
    }

    fun shouldRedirect(httpStatus: Int?, paramMap: MultiValueMap<String, String>): Boolean {
        httpStatus ?: return false

        if (httpStatus != HttpStatus.SC_MOVED_TEMPORARILY && httpStatus != HttpStatus.SC_MOVED_PERMANENTLY) {
            return false
        }

        return paramMap.contains(ParamEnum.AUTO_REDIRECT.param)
    }

    fun resolveLocationUrl(url: String, headers: HttpHeaders): String {
        val location = headers.firstValue(com.google.common.net.HttpHeaders.LOCATION).get()
        if (location.startsWith("http")) {
            // 为绝对路径,直接返回
            return location
        }

        val uri = URI(url)
        val scheme = uri.scheme
        val host = uri.host
        val port = uri.port

        val portStr = if (port == -1) "" else ":$port"

        return "$scheme://$host$portStr$location"
    }

    fun saveResBodyToFile(path: String, byteArray: ByteArray): VirtualFile {
        var file = File(path)
        file = File(PathUtils.legalizeFilePath(file.parent), legalizeFileName(file.name))

        if (!file.parentFile.exists()) {
            Files.createDirectories(file.toPath())
        }

        ByteArrayInputStream(byteArray).use {
            Files.copy(it, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        logInfo("响应体已保存到 output path: $file")

        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)!!
    }

    fun saveResBodyToFile(
        httpInfo: HttpInfo,
        tabName: String,
        noLog: Boolean,
        project: Project,
    ): Pair<VirtualFile?, Boolean> {
        val content = httpInfo.byteArray ?: return Pair(null, false)
        if (content.isEmpty()) {
            return Pair(null, false)
        }

        val outputFilePathText = httpInfo.outputFilePathText
        if (outputFilePathText != null) {
            val resBodyFile = saveResBodyToFile(outputFilePathText, content)

            return Pair(resBodyFile, true)
        }

        val fileName = resolveFilename(httpInfo)

        val resBodyFile = saveResBodyToFile(content, tabName, fileName, noLog, project)

        return if (noLog) {
            Pair(resBodyFile, false)
        } else {
            Pair(resBodyFile, true)
        }
    }

    private fun resolveFilename(httpInfo: HttpInfo): String {
        val resHeaders = httpInfo.resHeaders
        if (resHeaders != null) {
            var optional = resHeaders.firstValue(com.google.common.net.HttpHeaders.CONTENT_DISPOSITION)
            if (optional.isPresent) {
                val split = optional.get().split(";")

                val fileName = split
                    .mapNotNull {
                        val tmp = it.trim()
                        if (tmp.startsWith("filename", true)) {
                            val name = StringUtil.unquoteString(tmp.split("=")[1])
                            return@mapNotNull URLDecoder.decode(name, StandardCharsets.UTF_8)
                        }

                        null
                    }
                    .firstOrNull()

                if (fileName != null) {
                    return fileName
                }
            }
        }

        val suffix = SimpleTypeEnum.getSuffix(httpInfo.type!!, httpInfo.contentType!!)

        val statusCode = httpInfo.statusCode
        val statusStr = if (statusCode != null) "$statusCode." else ""

        return DateFormatUtils.format(Date(), "yyyy-MM-dd'T'HHmmssSSS") + ".$statusStr" + suffix
    }

    fun getDocument(virtualFile: VirtualFile): Document {
        val fileDocumentManager = FileDocumentManager.getInstance()
        return computeReadAction { fileDocumentManager.getDocument(virtualFile)!! }
    }

    private fun saveResBodyToFile(
        content: ByteArray,
        tabName: String,
        fileName: String,
        noLog: Boolean,
        project: Project,
    ): VirtualFile {
        if (noLog) {
            val lightVirtualFile = LightVirtualFile(fileName)
            lightVirtualFile.charset = StandardCharsets.UTF_8
            lightVirtualFile.setBinaryContent(content)
            return lightVirtualFile
        }

        val dateHistoryDir = getDateHistoryDir(project)

        val resBodyDir = File(dateHistoryDir, legalizeFileName(tabName))
        if (!resBodyDir.exists()) {
            resBodyDir.mkdirs()
        }

        val file = File(resBodyDir, fileName)

        ByteArrayInputStream(content).use {
            Files.copy(it, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        logInfo("响应体已保存到文件: $file")

        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)!!
    }

    fun handleResponse(
        url: String,
        response: HttpResponse<ByteArray>?,
        httpReqDescList: MutableList<String>,
        httpResDescList: MutableList<String>,
        handler: HttpProcessHandler,
        targetMethodType: HttpRequestEnum,
        reqBody: Any?,
        jsResList: MutableList<String>,
    ) {
        if (response == null) {
            return
        }

        val resHeaders = response.headers()

        val cookies = CookieUtils.parseAll(url, resHeaders)

        var cookieSavePair: Pair<String, VirtualFile>? = null
        if (!handler.paramMap.containsKey(ParamEnum.NO_COOKIE_JAR.param)) {
            cookieSavePair = CookieUtils.saveCookiesToFile(cookies, handler.project)
        }

        val resHeaderList = convertResponseHeaders(resHeaders)

        val resBody = response.body()
        val httpResInfo = convertResponseBody(resBody, resHeaders)

        val statusCode = response.statusCode()

        var resList: List<String>
        try {
            resList = handler.jsExecutor.evalJsAfterRequest(
                url, reqBody, handler.jsAfterReq, httpResInfo, statusCode,
                resHeaders.map(), cookies, handler.httpFile.name, handler.httpDocument
            )
        } catch (e: JsScriptException) {
            handler.jsScriptException = e

            resList = e.list
        }

        jsResList.addAll(resList)

        val jsAfterExecuteResult = JsExecuteResult(jsResList, handler.jsScriptException)

        val versionDesc = MyPsiUtils.Companion.getVersionDesc(response.version())

        val commentTabName = "### ${handler.tabName}${CR_LF}"
        httpResDescList.add(commentTabName)

        if (handler.paramMap.containsKey(ParamEnum.VISUALIZE_TIMESTAMP.param)) {
            httpResDescList.add("# @${ParamEnum.VISUALIZE_TIMESTAMP.param}${CR_LF}")
        }

        val methodName = if (targetMethodType == HttpRequestEnum.CUSTOM) {
            handler.httpMethod.text
        } else {
            targetMethodType.name
        }

        httpResDescList.add(methodName + " " + response.uri() + " " + versionDesc + CR_LF)

        httpResDescList.addAll(resHeaderList)

        val simpleTypeEnum = httpResInfo.simpleTypeEnum
        val bodyBytes = httpResInfo.bodyBytes
        val bodyStr = httpResInfo.bodyStr
        val contentType = httpResInfo.contentType

        if (simpleTypeEnum.binary) {
            val size = Formats.formatFileSize(resBody.size.toLong())
            httpResDescList.add(NlsBundle.nls("res.binary.data", size))
        } else {
            if (bodyStr!!.length > RES_SIZE_LIMIT) {
                httpResDescList.add("")
            } else {
                httpResDescList.add(bodyStr)
            }
        }

        val httpInfo = HttpInfo(
            httpReqDescList, httpResDescList, simpleTypeEnum, bodyBytes,
            null, contentType, resHeaders, handler.resolveOutputFilePath(), cookieSavePair, statusCode,
            0, resBody.size, null, jsAfterExecuteResult, null
        )

        saveResBodyToFile(
            httpInfo, handler.tabName, handler.paramMap.containsKey(ParamEnum.NO_LOG.param), handler.project
        )
    }

}