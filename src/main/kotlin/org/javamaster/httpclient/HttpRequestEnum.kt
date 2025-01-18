package org.javamaster.httpclient

import com.intellij.psi.util.PsiUtilCore
import org.javamaster.httpclient.js.JsScriptExecutor
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.utils.HttpUtils.convertToResHeaderDescList
import org.javamaster.httpclient.utils.HttpUtils.convertToResPair
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Version
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

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
                .timeout(READ_TIMEOUT_SEC)
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
                .timeout(READ_TIMEOUT_SEC)
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
                .timeout(READ_TIMEOUT_SEC)
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
                .timeout(READ_TIMEOUT_SEC)
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

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun execute(
        url: String,
        version: Version,
        reqHttpHeaders: MutableMap<String, String>,
        reqBody: Any?,
        jsScriptStr: String?,
        jsScriptExecutor: JsScriptExecutor,
        httpReqDescList: MutableList<String>,
        tabName: String,
    ): HttpInfo {
        val start = System.currentTimeMillis()

        var bodyPublisher: HttpRequest.BodyPublisher? = null
        when (reqBody) {
            is String -> {
                bodyPublisher = BodyPublishers.ofString(reqBody)
            }

            is List<*> -> {
                @Suppress("UNCHECKED_CAST")
                bodyPublisher = BodyPublishers.ofByteArrays(reqBody as MutableIterable<ByteArray>)
            }

            else -> {
                logger.warn("未知类型:${reqBody?.javaClass}")
            }
        }

        val request = createRequest(url, version, reqHttpHeaders, bodyPublisher)

        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build()

        val commentTabName = "# $tabName\r\n"
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
            httpReqDescList.add("Content-Length: " + bodyPublisher.contentLength() + "\r\n")
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

        val response: HttpResponse<ByteArray>?
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        } catch (e: Exception) {
            return HttpInfo(httpReqDescList, mutableListOf(), null, null, e)
        }

        val size = response.body().size / 1024.0
        val consumeTimes = System.currentTimeMillis() - start

        val resHeaderList = convertToResHeaderDescList(response)

        val resPair = convertToResPair(response)

        val httpResDescList =
            mutableListOf("# status: ${response.statusCode()} 耗时: ${consumeTimes}ms 大小: ${size}kb\r\n")

        val evalJsRes = jsScriptExecutor.evalJsAfterRequest(jsScriptStr, response, resPair)
        if (!evalJsRes.isNullOrEmpty()) {
            httpResDescList.add("# 后置js执行结果:\r\n")
            httpResDescList.add("$evalJsRes")
        }

        httpResDescList.add(commentTabName)

        httpResDescList.add(request.method() + " " + response.uri().toString() + "\r\n")

        httpResDescList.addAll(resHeaderList)

        if (resPair.first != "image") {
            httpResDescList.add(String(resPair.second, StandardCharsets.UTF_8))
        }

        return HttpInfo(httpReqDescList, httpResDescList, resPair.first, resPair.second, null)
    }

    abstract fun createRequest(
        url: String,
        version: Version,
        reqHeaderMap: MutableMap<String, String>,
        bodyPublisher: HttpRequest.BodyPublisher?,
    ): HttpRequest

    companion object {
        val READ_TIMEOUT_SEC: Duration = Duration.ofSeconds(30)

        fun getInstance(httpMethod: HttpMethod): HttpRequestEnum {
            val elementType = PsiUtilCore.getElementType(httpMethod.firstChild)
            if (elementType === HttpTypes.POST) {
                return POST
            } else if (elementType === HttpTypes.GET) {
                return GET
            } else if (elementType === HttpTypes.DELETE) {
                return DELETE
            } else if (elementType === HttpTypes.PUT) {
                return PUT
            }
            throw UnsupportedOperationException()
        }
    }
}
