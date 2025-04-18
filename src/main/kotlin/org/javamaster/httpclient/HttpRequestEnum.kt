package org.javamaster.httpclient

import com.google.common.net.HttpHeaders
import com.intellij.openapi.util.text.Formats
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CONNECT_TIMEOUT
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.HttpUtils.READ_TIMEOUT
import org.javamaster.httpclient.utils.HttpUtils.getVersionDesc
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Version
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * Sending http request
 *
 * @author yudong
 */
enum class HttpRequestEnum {
    GET {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            val readTimeout = paramMap[ParamEnum.READ_TIMEOUT_NAME.param]?.toLong() ?: READ_TIMEOUT

            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(Duration.ofSeconds(readTimeout))
                .GET()
                .uri(URI.create(url))

            setHeaders(reqHeaderMap, builder)

            return builder.build()
        }
    },
    POST {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            val readTimeout = paramMap[ParamEnum.READ_TIMEOUT_NAME.param]?.toLong() ?: READ_TIMEOUT

            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(Duration.ofSeconds(readTimeout))
                .uri(URI.create(url))

            setHeaders(reqHeaderMap, builder)

            builder.POST(bodyPublisher)

            return builder.build()
        }
    },
    DELETE {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            val readTimeout = paramMap[ParamEnum.READ_TIMEOUT_NAME.param]?.toLong() ?: READ_TIMEOUT

            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(Duration.ofSeconds(readTimeout))
                .DELETE()
                .uri(URI.create(url))

            setHeaders(reqHeaderMap, builder)

            return builder.build()
        }
    },
    PUT {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            val readTimeout = paramMap[ParamEnum.READ_TIMEOUT_NAME.param]?.toLong() ?: READ_TIMEOUT

            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(Duration.ofSeconds(readTimeout))
                .uri(URI.create(url))

            setHeaders(reqHeaderMap, builder)

            builder.PUT(bodyPublisher)

            return builder.build()
        }
    },
    OPTIONS {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return buildOtherRequest(name, url, version, reqHeaderMap, paramMap)
        }
    },
    PATCH {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return buildOtherRequest(name, url, version, reqHeaderMap, paramMap)
        }
    },
    HEAD {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return buildOtherRequest(name, url, version, reqHeaderMap, paramMap)
        }
    },
    TRACE {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            return buildOtherRequest(name, url, version, reqHeaderMap, paramMap)
        }
    },
    WEBSOCKET {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }
    },
    DUBBO {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: Map<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }
    }
    ;

    fun setHeaders(reqHeaderMap: LinkedMultiValueMap<String, String>, builder: HttpRequest.Builder) {
        reqHeaderMap.forEach {
            val name = it.key
            it.value.forEach { value ->
                builder.header(name, value)
            }
        }
    }

    fun buildOtherRequest(
        methodName: String,
        url: String,
        version: Version,
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        paramMap: Map<String, String>,
    ): HttpRequest {
        val readTimeout = paramMap[ParamEnum.READ_TIMEOUT_NAME.param]?.toLong() ?: READ_TIMEOUT

        val builder = HttpRequest.newBuilder()
            .version(version)
            .timeout(Duration.ofSeconds(readTimeout))
            .method(methodName, BodyPublishers.noBody())
            .uri(URI.create(url))

        setHeaders(reqHeaderMap, builder)

        return builder.build()
    }

    fun execute(
        url: String,
        version: Version,
        reqHttpHeaders: LinkedMultiValueMap<String, String>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
        tabName: String,
        paramMap: Map<String, String>,
    ): CompletableFuture<HttpResponse<ByteArray>> {
        try {
            val pair = HttpUtils.convertToReqBodyPublisher(reqBody)

            val bodyPublisher = pair.first
            val multipartLength = pair.second

            val request = createRequest(url, version, reqHttpHeaders, bodyPublisher, paramMap)

            val connectTimeout = paramMap[ParamEnum.CONNECT_TIMEOUT_NAME.param]?.toLong() ?: CONNECT_TIMEOUT

            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build()

            val commentTabName = "### $tabName$CR_LF"
            httpReqDescList.add(commentTabName)

            httpReqDescList.add(request.method() + " " + request.uri() + " " + getVersionDesc(version) + CR_LF)

            request.headers()
                .map()
                .forEach { entry ->
                    entry.value.forEach {
                        httpReqDescList.add(entry.key + ": " + it + CR_LF)
                    }
                }

            val tmpLength = bodyPublisher.contentLength()

            val contentLength = if (tmpLength == -1L) multipartLength else tmpLength

            httpReqDescList.add("${HttpHeaders.CONTENT_LENGTH}: $contentLength$CR_LF")

            val size = Formats.formatFileSize(contentLength)

            httpReqDescList.add(0, "// ${NlsBundle.nls("req.size", size)}$CR_LF")

            httpReqDescList.add(CR_LF)

            val descList = HttpUtils.getReqBodyDesc(reqBody)

            httpReqDescList.addAll(descList)

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
        } catch (e: Throwable) {
            return CompletableFuture.failedFuture(e)
        }
    }

    abstract fun createRequest(
        url: String,
        version: Version,
        reqHeaderMap: LinkedMultiValueMap<String, String>,
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