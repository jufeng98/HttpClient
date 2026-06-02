package org.javamaster.httpclient.utils

import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.application
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.HttpStatus
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.consts.HttpConsts.Companion.RES_SIZE_LIMIT
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.model.HttpInfo
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.JsonUtils.formatJson
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpHeaders
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
        val contentType = resHeaders.firstValue(CONTENT_TYPE).getOrElse { ContentType.TEXT_PLAIN.mimeType }

        val simpleTypeEnum = SimpleTypeEnum.convertContentType(contentType)

        val bodyStr = if (simpleTypeEnum.binary) {
            null
        } else {
            val str = String(bodyBytes, StandardCharsets.UTF_8)

            if (simpleTypeEnum == SimpleTypeEnum.JSON) {
                if (bodyBytes.size > RES_SIZE_LIMIT) {
                    str
                } else {
                    val prettyStr = formatJson(str)
                    if (prettyStr.length > RES_SIZE_LIMIT) {
                        str
                    } else {
                        bodyBytes = prettyStr.toByteArray(StandardCharsets.UTF_8)

                        prettyStr
                    }
                }
            } else {
                str
            }
        }

        return HttpResInfo(simpleTypeEnum, bodyBytes, bodyStr, contentType)
    }

    fun shouldRedirect(httpStatus: Int?, paramMap: Map<String, String>): Boolean {
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

    fun saveResToFile(path: String, byteArray: ByteArray): String {
        var file = File(path)
        file = File(PathUtils.legalizeFilePath(file.parent), PathUtils.legalizeFileName(file.name))

        if (!file.parentFile.exists()) {
            Files.createDirectories(file.toPath())
        }

        try {
            ByteArrayInputStream(byteArray).use {
                Files.copy(it, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "// ${nls("save.failed")}: $e$CR_LF"
        }

        application.executeOnPooledThread {
            VirtualFileManager.getInstance().refreshAndFindFileByNioPath(file.toPath())
        }

        return "// ${nls("save.to.file", file.normalize().absolutePath)}$CR_LF"
    }

    fun resolveFilename(httpInfo: HttpInfo): String {
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

        val suffix = SimpleTypeEnum.Companion.getSuffix(httpInfo.type!!, httpInfo.contentType!!)
        return DateFormatUtils.format(Date(), "yyyy-MM-dd'T'HHmmss") + "." + suffix
    }

}