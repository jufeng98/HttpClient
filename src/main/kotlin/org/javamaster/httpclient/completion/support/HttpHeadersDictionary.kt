package org.javamaster.httpclient.completion.support

import com.google.common.net.HttpHeaders
import com.google.common.net.HttpHeaders.ReferrerPolicyValues
import com.google.gson.JsonParser
import com.intellij.openapi.util.io.FileUtil
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.doc.support.HttpHeaderDocumentation
import org.javamaster.httpclient.doc.support.HttpHeaderDocumentation.Companion.read
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.DubboUtils

/**
 * @author yudong
 */
object HttpHeadersDictionary {
    private val encodingValues = listOf(
        "compress",
        "deflate",
        "exi",
        "gzip",
        "identity",
        "pack200-gzip",
        "br",
        "bzip2",
        "lzma",
        "peerdist",
        "sdch",
        "xpress",
        "xz"
    )

    private val predefinedMimeVariants = listOf(
        "application/json",
        "application/xml",
        "application/x-yaml",
        "application/graphql",
        "application/atom+xml",
        "application/xhtml+xml",
        "application/svg+xml",
        "application/sql",
        "application/pdf",
        "application/zip",
        "application/x-www-form-urlencoded",
        "multipart/form-data",
        "application/octet-stream",
        "text/plain",
        "text/xml",
        "text/html",
        "text/json",
        "text/csv",
        "image/png",
        "image/jpeg",
        "image/gif",
        "image/webp",
        "image/svg+xml",
        "audio/mpeg",
        "audio/vorbis",
        "text/event-stream",
        "application/stream+json",
        "application/x-ndjson",
        ContentType.MULTIPART_FORM_DATA.mimeType + "; boundary=----WebBoundary"
    )

    private val secWebsocketProtocolValues by lazy {
        listOf("graphql-ws", "subscriptions-transport-ws", "aws-app-sync")
    }

    private val referrerPolicyValues by lazy {
        val fields = ReferrerPolicyValues::class.java.declaredFields
        fields.map {
            it.isAccessible = true
            it[null] as String
        }
    }

    private val knownExtraHeaders = listOf(
        "X-Correlation-ID",
        "X-Csrf-Token",
        "X-Forwarded-For",
        "X-Forwarded-Host",
        "X-Forwarded-Proto",
        "X-Http-Method-Override",
        "X-Request-ID",
        "X-Requested-With",
        "X-Total-Count",
        "X-User-Agent",
        "Admin-Token",
        HttpHeaders.REFERRER_POLICY,
    )

    val headerMap by lazy {
        val map = createMapFromFile()

        for (header in knownExtraHeaders) {
            map[header] = HttpHeaderDocumentation(header)
        }

        map
    }

    val dubboHeaderNames by lazy {
        listOf(
            HttpHeaders.CONTENT_TYPE,
            DubboUtils.INTERFACE_KEY,
            DubboUtils.INTERFACE_NAME,
            DubboUtils.METHOD_KEY,
            DubboUtils.VERSION,
            DubboUtils.REGISTRY,
        )
    }

    val headerValuesMap by lazy {
        val map = mutableMapOf<String, List<String>>()
        map[HttpHeaders.ACCEPT_ENCODING] = encodingValues
        map[HttpHeaders.CONTENT_TYPE] = predefinedMimeVariants
        map[HttpHeaders.ACCEPT] = predefinedMimeVariants
        map[HttpHeaders.REFERRER_POLICY] = referrerPolicyValues
        map[HttpHeaders.SEC_WEBSOCKET_PROTOCOL] = secWebsocketProtocolValues
        map
    }

    fun getDocumentation(fieldName: String): HttpHeaderDocumentation? {
        return headerMap[fieldName]
    }

    private fun createMapFromFile(): MutableMap<String, HttpHeaderDocumentation> {
        val name = "doc/header-documentation_${NlsBundle.lang}.json"
        val stream = HttpHeadersDictionary::class.java.classLoader.getResourceAsStream(name)!!

        val jsonText = FileUtil.loadTextAndClose(stream)

        val jsonElement = JsonParser.parseString(jsonText)

        if (!jsonElement.isJsonArray) return mutableMapOf()

        val map = mutableMapOf<String, HttpHeaderDocumentation>()

        for (element in jsonElement.asJsonArray) {
            if (!element.isJsonObject) continue

            val documentation = read(element.asJsonObject) ?: continue

            map[documentation.name] = documentation
        }

        return map
    }
}
