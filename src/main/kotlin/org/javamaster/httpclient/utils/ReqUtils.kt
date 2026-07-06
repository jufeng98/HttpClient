package org.javamaster.httpclient.utils

import com.google.common.net.HttpHeaders
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import org.apache.http.entity.ContentType
import org.intellij.markdown.html.urlEncode
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts.Companion.VAR_BRACE_END
import org.javamaster.httpclient.consts.HttpConsts.Companion.VAR_BRACE_START
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.exception.BodyUnresolvedVariableException
import org.javamaster.httpclient.js.JsExecutor
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import org.javamaster.httpclient.map.MultiValueMap
import org.javamaster.httpclient.model.PreJsFile
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import java.net.http.HttpClient
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.function.IntConsumer
import kotlin.jvm.optionals.getOrNull

/**
 * @author yudong
 */
class ReqUtils {

    companion object {

        fun convertToReqBodyPublisher(reqBody: Any?): Pair<BodyPublisher, Long> {
            if (reqBody == null) {
                return Pair(BodyPublishers.noBody(), 0L)
            }

            var multipartLength = 0L
            val bodyPublisher: BodyPublisher

            when (reqBody) {
                is Triple<*, *, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val triple = reqBody as Triple<ByteArray?, String?, ContentType?>

                    val first = triple.first
                    if (first != null) {
                        bodyPublisher = BodyPublishers.ofByteArray(first)
                    } else {
                        bodyPublisher = BodyPublishers.noBody()
                    }
                }

                is MutableList<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val list = reqBody as MutableList<Triple<ByteArray?, String?, ContentType?>>

                    val byteArrays = list.mapNotNull { it.first }

                    bodyPublisher = BodyPublishers.ofByteArrays(byteArrays)

                    multipartLength = byteArrays.sumOf { it.size.toLong() }
                }

                else -> {
                    logWarn(NlsBundle.nls("reqBody.unknown", reqBody.javaClass))

                    bodyPublisher = BodyPublishers.noBody()
                }
            }

            return Pair(bodyPublisher, multipartLength)
        }

        fun convertReqBody(reqBody: Any?): Any? {
            if (reqBody == null) {
                return null
            }

            return when (reqBody) {
                is Triple<*, *, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val triple = reqBody as Triple<ByteArray?, String?, ContentType?>

                    if (HttpUtils.isTxtContentType(triple.third)) {
                        triple.second
                    } else {
                        triple.first
                    }
                }

                is MutableList<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val list = reqBody as MutableList<Triple<ByteArray?, String?, ContentType?>>

                    list.mapNotNull { it.first }.reduce { a, b -> a + b }
                }

                else -> {
                    throw IllegalArgumentException(NlsBundle.nls("reqBody.unknown", reqBody.javaClass))
                }
            }
        }

        fun resolveReqBodyAgain(
            reqBody: Any?,
            resolver: VariableResolver,
            paramMap: MultiValueMap<String, String>,
        ): Any? {
            if (reqBody == null) {
                return null
            }

            if (reqBody is Triple<*, *, *>) {
                @Suppress("UNCHECKED_CAST")
                val triple = reqBody as Triple<ByteArray?, String?, ContentType?>

                val contentType = triple.third
                if (HttpUtils.isTxtContentType(contentType)) {
                    val second = triple.second
                    if (second != null) {
                        var resolved = resolver.resolve(second)

                        val variableName = extractVariable(resolved)
                        if (variableName != null) {
                            throw BodyUnresolvedVariableException(variableName)
                        }

                        val formUrlEncodeReq = contentType?.mimeType == ContentType.APPLICATION_FORM_URLENCODED.mimeType
                        val shouldEncode = formUrlEncodeReq && paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)
                        if (shouldEncode) {
                            resolved = encodeQueryParam(resolved)
                        }

                        val charset = contentType?.charset ?: StandardCharsets.UTF_8
                        return Triple(resolved.toByteArray(charset), resolved, contentType)
                    } else {
                        return triple
                    }
                } else {
                    return triple
                }
            } else if (reqBody is MutableList<*>) {
                @Suppress("UNCHECKED_CAST")
                val list = reqBody as MutableList<Triple<ByteArray?, String?, ContentType?>>

                val newList = mutableListOf<Triple<ByteArray?, String?, ContentType?>>()

                for (triple in list) {
                    val contentType = triple.third

                    if (!HttpUtils.isTxtContentType(contentType)) {
                        newList.add(triple)
                        continue
                    }

                    val content = triple.second
                    if (content == null) {
                        newList.add(triple)
                        continue
                    }

                    var variableName = extractVariable(content)
                    if (variableName == null) {
                        newList.add(triple)
                        continue
                    }

                    var resolved = resolver.resolve(content)

                    variableName = extractVariable(resolved)
                    if (variableName != null) {
                        throw BodyUnresolvedVariableException(variableName)
                    }

                    val formUrlEncodeReq = contentType?.mimeType == ContentType.APPLICATION_FORM_URLENCODED.mimeType
                    val shouldEncode = formUrlEncodeReq && paramMap.containsKey(ParamEnum.AUTO_ENCODING.param)
                    if (shouldEncode) {
                        // 先移除尾部的\r\n
                        resolved = resolved.trimEnd()
                        resolved = encodeQueryParam(resolved)
                        // 重新加回尾部的\r\n
                        resolved = resolved + CR_LF
                    }

                    val charset = contentType?.charset ?: StandardCharsets.UTF_8
                    newList.add(Triple(resolved.toByteArray(charset), resolved, contentType))
                }

                return newList
            }

            return reqBody
        }

        fun extractVariable(content: String): String? {
            val idxStart = content.indexOf(VAR_BRACE_START)
            if (idxStart == -1) return null

            val idxEnd = content.indexOf(VAR_BRACE_END, idxStart)
            if (idxEnd == -1) return null

            return content.substring(idxStart + VAR_BRACE_START.length, idxEnd)
        }

        fun getReqBodyDesc(reqBody: Any?): MutableList<String> {
            val descList = mutableListOf<String>()

            when (reqBody) {
                is Triple<*, *, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val triple = reqBody as Triple<ByteArray?, String?, ContentType?>

                    descList.addAll(convertTriple(triple))
                }

                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val list = reqBody as MutableList<Triple<ByteArray?, String?, ContentType?>>

                    list.forEach {
                        descList.addAll(convertTriple(it))
                    }
                }
            }

            return descList
        }

        private fun convertTriple(triple: Triple<ByteArray?, String?, ContentType?>): MutableList<String> {
            val descList = mutableListOf<String>()

            val maxSizeLimit = 50000

            val first = triple.first
            val second = triple.second
            val third = triple.third
            if (HttpUtils.isTxtContentType(third)) {
                if (second != null) {
                    if (second.length > maxSizeLimit) {
                        val part =
                            second.substring(0, maxSizeLimit) + "$CR_LF......(${NlsBundle.nls("content.truncated")})"
                        descList.add(part)
                    } else {
                        descList.add(second)
                    }
                }
            } else {
                if (first != null) {
                    descList.add(second!!)
                }
            }

            return descList
        }

        fun initPreJsFilesContent(preJsFiles: List<PreJsFile>, project: Project, httpFile: HttpFile) {
            preJsFiles.forEach {
                if (JsExecutor.isLibraryLoaded(it.file.absolutePath)) {
                    return@forEach
                }

                try {
                    val content = VirtualFileUtils.readNewestContent(it.virtualFile)
                    it.content = content
                } catch (e: Exception) {
                    val document = PsiDocumentManager.getInstance(project).getDocument(httpFile)!!
                    val rowNum = document.getLineNumber(it.directionComment.textOffset) + 1

                    throw RuntimeException("$e(${httpFile.name}#${rowNum})", e)
                }
            }
        }

        fun handleUrl(url: String): String {
            val split = url.split("?")
            if (split.size == 1) {
                return url
            }

            return split[0] + "?" + removeQueryParamSpaceAndCr(split[1])
        }

        fun encodeUrl(url: String): String {
            val split = url.split("?")
            if (split.size == 1) {
                return url
            }

            return split[0] + "?" + encodeQueryParam(split[1])
        }

        /**
         * 移除查询参数多余的空格和换行符
         */
        fun removeQueryParamSpaceAndCr(queryParam: String): String {
            val split = queryParam.split("&")

            return split.joinToString("&") {
                val list = it.split("=")

                val oneSize = list.size == 1
                if (oneSize && list[0].startsWith(VAR_BRACE_START)) {
                    return@joinToString list[0]
                }

                if (oneSize) {
                    return@joinToString list[0].trim()
                }

                list[0].trim() + "=" + list[1].trim()
            }
        }

        fun encodeQueryParam(queryParam: String): String {
            val split = queryParam.split("&")

            return split.joinToString("&") {
                val list = it.split("=")

                if (list.size > 1) urlEncode(list[0]) + "=" + urlEncode(list[1]) else urlEncode(list[0])
            }
        }

        fun getContentLength(
            url: String,
            version: HttpClient.Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            paramMap: MultiValueMap<String, String>,
            consumer: IntConsumer,
        ) {
            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build()

            val headRequest = HttpRequestEnum.HEAD.createRequest(
                url, version, reqHeaderMap, BodyPublishers.noBody(), paramMap
            )

            client.sendAsync(headRequest, HttpResponse.BodyHandlers.ofByteArray())
                .whenComplete { response, throwable ->
                    if (throwable != null) {
                        logWarn("获取长度错误", throwable)
                        consumer.accept(-1)
                    } else {
                        val length = response.headers().firstValue(HttpHeaders.CONTENT_LENGTH).getOrNull()
                        if (length == null) {
                            consumer.accept(-1)
                        } else {
                            consumer.accept(length.toInt())
                        }
                    }
                }
        }
    }
}
