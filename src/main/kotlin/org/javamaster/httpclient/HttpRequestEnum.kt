package org.javamaster.httpclient

import com.google.common.net.HttpHeaders
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ex.ApplicationInfoEx
import org.javamaster.httpclient.consts.HttpConsts.Companion.CONNECT_TIMEOUT
import org.javamaster.httpclient.consts.HttpConsts.Companion.READ_TIMEOUT
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.handler.ProgressBodyHandler
import org.javamaster.httpclient.map.MultiValueMap
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
import java.util.function.IntConsumer
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
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).GET().build()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(httpList)
        }
    },
    POST(HttpIcons.POST) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).POST(bodyPublisher).build()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(httpList)
        }
    },
    DELETE(HttpIcons.DELETE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, bodyPublisher).build()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(httpList)
        }
    },
    PUT(HttpIcons.PUT) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).PUT(bodyPublisher).build()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(httpList)
        }
    },
    OPTIONS(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, BodyPublishers.noBody()).build()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(httpList)
        }
    },
    PATCH(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, bodyPublisher).build()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(httpList)
        }
    },
    HEAD(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, BodyPublishers.noBody()).build()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(httpList)
        }
    },
    TRACE(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            return createBuilder(url, reqHeaderMap, version, paramMap).method(name, BodyPublishers.noBody()).build()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(httpList)
        }
    },
    WEBSOCKET(HttpIcons.WS) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(wsList)
        }
    },
    DUBBO(HttpIcons.DUBBO) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(dubboList)
        }
    },
    MOCK_SERVER(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(httpList)
        }
    },
    MOCK_WS(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(wsList)
        }
    },
    MOCK_DUBBO(HttpIcons.FILE) {
        override fun createRequest(
            url: String,
            version: Version,
            reqHeaderMap: MultiValueMap<String, String?>,
            bodyPublisher: HttpRequest.BodyPublisher,
            paramMap: MultiValueMap<String, String>,
        ): HttpRequest {
            throw UnsupportedOperationException()
        }

        override fun getVariants(): List<LookupElementBuilder> {
            return getVariants(dubboList)
        }
    }
    ;

    internal fun createBuilder(
        url: String,
        reqHeaderMap: MultiValueMap<String, String?>,
        version: Version,
        paramMap: MultiValueMap<String, String>,
    ): HttpRequest.Builder {
        val readTimeout = paramMap.getFirst(ParamEnum.READ_TIMEOUT_NAME.param)?.toLong() ?: READ_TIMEOUT

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
        reqHttpHeaders: MultiValueMap<String, String?>,
        reqBody: Any?,
        httpReqDescList: MutableList<String>,
        tabName: String,
        paramMap: MultiValueMap<String, String>,
    ): Pair<HttpRequest, Long> {
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

        httpReqDescList.add("### $tabName$CR_LF")

        if (paramMap.containsKey(ParamEnum.VISUALIZE_TIMESTAMP.param)) {
            httpReqDescList.add("# @${ParamEnum.VISUALIZE_TIMESTAMP.param}$CR_LF")
        }

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

        httpReqDescList.add(CR_LF)

        val descList = ReqUtils.getReqBodyDesc(reqBody)

        httpReqDescList.addAll(descList)

        return Pair(request, contentLength)
    }

    fun execute(
        paramMap: MultiValueMap<String, String>,
        req: HttpRequest,
        progressCallback: IntConsumer,
    ): CompletableFuture<HttpResponse<ByteArray>> {
        try {
            val connectTimeout = paramMap.getFirst(ParamEnum.CONNECT_TIMEOUT_NAME.param)?.toLong() ?: CONNECT_TIMEOUT

            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build()

            val progressBodyHandler = ProgressBodyHandler.ofProgress(
                1024, HttpResponse.BodyHandlers.ofByteArray(), progressCallback
            )

            return client.sendAsync(req, progressBodyHandler)
        } catch (e: Throwable) {
            return CompletableFuture.failedFuture(e)
        }
    }

    abstract fun createRequest(
        url: String,
        version: Version,
        reqHeaderMap: MultiValueMap<String, String?>,
        bodyPublisher: HttpRequest.BodyPublisher,
        paramMap: MultiValueMap<String, String>,
    ): HttpRequest

    abstract fun getVariants(): List<LookupElementBuilder>

    companion object {
        @Suppress("HttpUrlsUsage")
        private val httpList = listOf("http://", "https://")
        private val wsList = listOf("ws://", "wss://")
        private val dubboList = listOf("dubbo://")

        private fun getVariants(schemaList: List<String>): List<LookupElementBuilder> {
            val list = mutableListOf<LookupElementBuilder>()

            schemaList.forEach { list.add(LookupElementBuilder.create(it)) }
            schemaList.forEach { list.add(LookupElementBuilder.create(it + "localhost")) }
            schemaList.forEach { list.add(LookupElementBuilder.create(it + "localhost:8080")) }

            return list
        }

        fun getInstance(methodName: String): HttpRequestEnum {
            try {
                return HttpRequestEnum.valueOf(methodName)
            } catch (e: Exception) {
                throw UnsupportedOperationException(NlsBundle.nls("method.unsupported", methodName), e)
            }
        }

    }
}