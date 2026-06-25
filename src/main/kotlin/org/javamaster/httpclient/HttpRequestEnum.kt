package org.javamaster.httpclient

import com.google.common.net.HttpHeaders
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.util.text.Formats
import org.javamaster.httpclient.consts.HttpConsts.Companion.CONNECT_TIMEOUT
import org.javamaster.httpclient.consts.HttpConsts.Companion.READ_TIMEOUT
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.MyPsiUtils.Companion.getVersionDesc
import org.javamaster.httpclient.utils.ReqUtils
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Version
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import javax.swing.Icon

/**
 * Sending http request
 *
 * @author yudong
 */
enum class HttpRequestEnum(val icon: Icon) {
    GET(HttpIcons.GET) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).GET().build()
        }
    },
    POST(HttpIcons.POST) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).POST(bodyPublisher).build()
        }
    },
    DELETE(HttpIcons.DELETE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, bodyPublisher).build()
        }
    },
    PUT(HttpIcons.PUT) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).PUT(bodyPublisher).build()
        }
    },
    OPTIONS(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, BodyPublishers.noBody()).build()
        }
    },
    PATCH(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, bodyPublisher).build()
        }
    },
    HEAD(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, BodyPublishers.noBody()).build()
        }
    },
    TRACE(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, BodyPublishers.noBody()).build()
        }
    },
    WEBSOCKET(HttpIcons.WS) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }
    },
    DUBBO(HttpIcons.DUBBO) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }
    },
    MOCK_SERVER(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }
    }
    ;

    internal fun createBuilder(
        url: String,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
        version: Version,
        paramMap: Map<String, String>,
    ): HttpRequest.Builder {
        val readTimeout = paramMap[ParamEnum.READ_TIMEOUT_NAME.param]?.toLong() ?: READ_TIMEOUT

        val builder = HttpRequest.newBuilder()
            .version(version)
            .timeout(Duration.ofSeconds(readTimeout))
            .uri(URI.create(url))

        reqHeaderMap.forEach {
            val name = it.key
            it.value.forEach { value ->
                builder.header(name, value ?: "null")
            }
        }

        return builder
    }

    fun preExecute(
        url: String,
        version: Version,
        reqHttpHeaders: LinkedMultiValueMap<String, String?>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
        tabName: String,
        paramMap: Map<String, String>,
    ): HttpRequest {
        val pair = ReqUtils.convertToReqBodyPublisher(reqBody)

        val bodyPublisher = pair.first
        val multipartLength = pair.second

        if (!reqHttpHeaders.contains(HttpHeaders.USER_AGENT)) {
            val appInfo = ApplicationInfoEx.getInstanceEx()
            reqHttpHeaders.add(HttpHeaders.USER_AGENT, "IntelliJ HTTP Request/${appInfo.fullApplicationName}")
        }

        if (!reqHttpHeaders.contains(HttpHeaders.ACCEPT_ENCODING)) {
            reqHttpHeaders.add(HttpHeaders.ACCEPT_ENCODING, "deflate, gzip, x-gzip")
        }

        if (!reqHttpHeaders.contains(HttpHeaders.ACCEPT)) {
            reqHttpHeaders.add(HttpHeaders.ACCEPT, "*/*")
        }

        val request = createRequest(url, version, reqHttpHeaders, bodyPublisher, paramMap)

        var insertIdx = 1
        httpReqDescList.add("### $tabName$CR_LF")

        if (paramMap.containsKey(ParamEnum.VISUALIZE_TIMESTAMP.param)) {
            insertIdx++
            httpReqDescList.add("# @${ParamEnum.VISUALIZE_TIMESTAMP.param}$CR_LF")
        }

        insertIdx++
        httpReqDescList.add(request.method() + " " + request.uri() + " " + getVersionDesc(version) + CR_LF)

        request.headers()
            .map()
            .forEach { entry ->
                entry.value.forEach {
                    insertIdx++
                    httpReqDescList.add(entry.key + ": " + it + CR_LF)
                }
            }

        val tmpLength = bodyPublisher.contentLength()

        val contentLength = if (tmpLength == -1L) multipartLength else tmpLength

        insertIdx++
        httpReqDescList.add("${HttpHeaders.CONTENT_LENGTH}: $contentLength$CR_LF")

        val size = Formats.formatFileSize(contentLength)

        httpReqDescList.add(
            httpReqDescList.size - insertIdx,
            "// ${NlsBundle.nls("req.size", contentLength, size)}$CR_LF"
        )

        httpReqDescList.add(CR_LF)

        val descList = ReqUtils.getReqBodyDesc(reqBody)

        httpReqDescList.addAll(descList)

        return request
    }

    fun execute(paramMap: Map<String, String>, req: HttpRequest): CompletableFuture<HttpResponse<ByteArray>> {
        try {
            val connectTimeout = paramMap[ParamEnum.CONNECT_TIMEOUT_NAME.param]?.toLong() ?: CONNECT_TIMEOUT

            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build()

            return client.sendAsync(req, HttpResponse.BodyHandlers.ofByteArray())
        } catch (e: Throwable) {
            return CompletableFuture.failedFuture(e)
        }
    }

    abstract fun createRequest(
        url: String,
        version: Version,
        reqHeaderMap: LinkedMultiValueMap<String, String?>,
        bodyPublisher: HttpRequest.BodyPublisher,
        paramMap: Map<String, String>,
    ): HttpRequest

    companion object {

        fun getInstance(methodName: String): HttpRequestEnum {
            try {
                return HttpRequestEnum.valueOf(methodName)
            } catch (e: Exception) {
                throw UnsupportedOperationException(NlsBundle.nls("method.unsupported", methodName), e)
            }
        }

    }
}