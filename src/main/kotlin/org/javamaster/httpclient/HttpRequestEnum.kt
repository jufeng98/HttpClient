package org.javamaster.httpclient

import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.map.LinkedMultiValueMap
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.HttpUtils.CONNECT_TIMEOUT
import org.javamaster.httpclient.utils.HttpUtils.READ_TIMEOUT
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
            bodyPublisher: HttpRequest.BodyPublisher?,
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
            bodyPublisher: HttpRequest.BodyPublisher?,
            paramMap: Map<String, String>,
        ): HttpRequest {
            val readTimeout = paramMap[ParamEnum.READ_TIMEOUT_NAME.param]?.toLong() ?: READ_TIMEOUT

            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(Duration.ofSeconds(readTimeout))
                .uri(URI.create(url))

            setHeaders(reqHeaderMap, builder)

            if (bodyPublisher != null) {
                builder.POST(bodyPublisher)
            } else {
                builder.POST(BodyPublishers.noBody())
            }

            return builder.build()
        }
    },
    DELETE {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher?,
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
            bodyPublisher: HttpRequest.BodyPublisher?,
            paramMap: Map<String, String>,
        ): HttpRequest {
            val readTimeout = paramMap[ParamEnum.READ_TIMEOUT_NAME.param]?.toLong() ?: READ_TIMEOUT

            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(Duration.ofSeconds(readTimeout))
                .uri(URI.create(url))

            setHeaders(reqHeaderMap, builder)

            if (bodyPublisher != null) {
                builder.PUT(bodyPublisher)
            } else {
                builder.PUT(BodyPublishers.noBody())
            }

            return builder.build()
        }
    },
    OPTIONS {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: LinkedMultiValueMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher?,
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
            bodyPublisher: HttpRequest.BodyPublisher?,
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
            bodyPublisher: HttpRequest.BodyPublisher?,
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
            bodyPublisher: HttpRequest.BodyPublisher?,
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
            bodyPublisher: HttpRequest.BodyPublisher?,
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
            bodyPublisher: HttpRequest.BodyPublisher?,
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
            var multipartLength = 0L
            var bodyPublisher: HttpRequest.BodyPublisher? = null
            when (reqBody) {
                is String -> {
                    bodyPublisher = BodyPublishers.ofString(reqBody)
                }

                is ByteArray -> {
                    bodyPublisher = BodyPublishers.ofByteArray(reqBody)
                }

                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    bodyPublisher = BodyPublishers.ofByteArrays(reqBody as MutableIterable<ByteArray>)
                    reqBody.forEach { multipartLength += it.size }
                }

                else -> {
                    if (reqBody != null) {
                        println("Unknown type: ${reqBody.javaClass}")
                    }
                }
            }

            val request = createRequest(url, version, reqHttpHeaders, bodyPublisher, paramMap)

            val connectTimeout = paramMap[ParamEnum.CONNECT_TIMEOUT_NAME.param]?.toLong() ?: CONNECT_TIMEOUT
            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build()

            val commentTabName = "### $tabName\r\n"
            httpReqDescList.add(commentTabName)

            httpReqDescList.add(request.method() + " " + request.uri().toString() + " " + "\r\n")
            request.headers()
                .map()
                .forEach { entry ->
                    entry.value.forEach {
                        httpReqDescList.add(entry.key + ": " + it + "\r\n")
                    }
                }

            if (bodyPublisher != null) {
                val tmpLength = bodyPublisher.contentLength()
                val contentLength = if (tmpLength != -1L) {
                    tmpLength
                } else {
                    multipartLength
                }
                httpReqDescList.add("Content-Length: $contentLength\r\n")

                val contentLengthKb = contentLength / 1024.0
                httpReqDescList.add(0, "// Size: $contentLengthKb KB\r\n")
            }
            httpReqDescList.add("\r\n")

            if (reqBody is String) {
                val max = 50000
                if (reqBody.length > max) {
                    httpReqDescList.add(
                        reqBody.substring(
                            0,
                            max
                        ) + "\r\n......(${NlsBundle.nls("content.truncated")})"
                    )
                } else {
                    httpReqDescList.add(reqBody)
                }
            } else if (reqBody is List<*>) {
                @Suppress("UNCHECKED_CAST")
                val byteArrays = reqBody as MutableIterable<ByteArray>
                byteArrays.forEach {
                    val max = 20000
                    if (it.size > max) {
                        val bytes = it.copyOfRange(0, max)
                        httpReqDescList.add(String(bytes) + " \r\n......(${NlsBundle.nls("content.truncated")})\r\n")
                    } else {
                        httpReqDescList.add(String(it))
                    }
                }
            }

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
        } catch (e: Throwable) {
            return CompletableFuture.failedFuture(e)
        }
    }

    abstract fun createRequest(
        url: String,
        version: Version,
        reqHeaderMap: LinkedMultiValueMap<String, String>,
        bodyPublisher: HttpRequest.BodyPublisher?,
        paramMap: Map<String, String>,
    ): HttpRequest

    companion object {

        fun getInstance(methodName: String): HttpRequestEnum {
            try {
                return HttpRequestEnum.valueOf(methodName)
            } catch (e: Exception) {
                throw UnsupportedOperationException("Method not supported: $methodName", e)
            }
        }

    }
}