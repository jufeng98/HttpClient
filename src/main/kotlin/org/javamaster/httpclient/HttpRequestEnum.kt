package org.javamaster.httpclient

import org.javamaster.httpclient.psi.HttpMethod
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Version
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * 发起 http 请求
 *
 * @author yudong
 */
enum class HttpRequestEnum {
    GET {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MutableMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher?,
        ): HttpRequest {
            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(READ_TIMEOUT)
                .GET()
                .uri(URI.create(url))

            reqHeaderMap.forEach(builder::setHeader)

            return builder.build()
        }
    },
    POST {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MutableMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher?,
        ): HttpRequest {
            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(READ_TIMEOUT)
                .uri(URI.create(url))

            reqHeaderMap.forEach(builder::setHeader)

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
            reqHeaderMap: MutableMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher?,
        ): HttpRequest {
            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(READ_TIMEOUT)
                .DELETE()
                .uri(URI.create(url))

            reqHeaderMap.forEach(builder::setHeader)

            return builder.build()
        }
    },
    PUT {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MutableMap<String, String>,
            bodyPublisher: HttpRequest.BodyPublisher?,
        ): HttpRequest {
            val builder = HttpRequest.newBuilder()
                .version(version)
                .timeout(READ_TIMEOUT)
                .uri(URI.create(url))

            reqHeaderMap.forEach(builder::setHeader)

            if (bodyPublisher != null) {
                builder.PUT(bodyPublisher)
            } else {
                builder.PUT(BodyPublishers.noBody())
            }

            return builder.build()
        }
    },
    ;

    fun execute(
        url: String,
        version: Version,
        reqHttpHeaders: MutableMap<String, String>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
        tabName: String,
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
                    println("未知类型:${reqBody?.javaClass}")
                }
            }

            val request = createRequest(url, version, reqHttpHeaders, bodyPublisher)

            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
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
                val contentLengthKb = contentLength / 1024.0
                httpReqDescList.add("Content-Length: $contentLength // $contentLengthKb KB\r\n")
            }
            httpReqDescList.add("\r\n")

            if (reqBody is String) {
                val max = 50000
                if (reqBody.length > max) {
                    httpReqDescList.add(reqBody.substring(0, max) + "\r\n......(内容过长,已截断显示)")
                } else {
                    httpReqDescList.add(reqBody)
                }
            } else if (reqBody is List<*>) {
                @Suppress("UNCHECKED_CAST")
                val byteArrays = reqBody as MutableIterable<ByteArray>
                byteArrays.forEach {
                    val max = 10000
                    if (it.size > max) {
                        val bytes = it.copyOfRange(0, max)
                        httpReqDescList.add(String(bytes) + " \r\n......(内容过长,已截断显示)\r\n")
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
        reqHeaderMap: MutableMap<String, String>,
        bodyPublisher: HttpRequest.BodyPublisher?,
    ): HttpRequest

    companion object {
        // 加了请求中断功能,因此设为永不超时
        val READ_TIMEOUT: Duration = Duration.ofDays(1)

        fun getInstance(httpMethod: HttpMethod): HttpRequestEnum {
            val name = httpMethod.text
            try {
                return HttpRequestEnum.valueOf(name)
            } catch (e: Exception) {
                throw UnsupportedOperationException("方法不受支持:$name", e)
            }
        }
    }
}