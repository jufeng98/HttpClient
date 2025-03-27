package org.javamaster.httpclient.completion.support

import org.apache.http.HttpHeaders
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.utils.DubboUtils

object HttpHeadersDictionary {

    val contentTypeValues by lazy {
        val field = ContentType::class.java.getDeclaredField("CONTENT_TYPE_MAP")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val map = field.get(null) as Map<String, ContentType>

        val list = mutableListOf(ContentType.MULTIPART_FORM_DATA.mimeType + "; boundary=----WebBoundary")
        list.addAll(map.keys)

        list.toTypedArray()
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

    val myWebSocketProtocols by lazy {
        listOf("graphql-ws", "subscriptions-transport-ws", "aws-app-sync")
    }

    val headerNameMap: MutableMap<String, HttpHeaderDocumentation> by lazy {
        val map: MutableMap<String, HttpHeaderDocumentation> = mutableMapOf()
        val fields = HttpHeaders::class.java.declaredFields
        for (field in fields) {
            field.isAccessible = true
            val value = field[null] as String
            map[value] = HttpHeaderDocumentation(value, false)
        }

        var header = com.google.common.net.HttpHeaders.CONTENT_DISPOSITION
        map[header] = HttpHeaderDocumentation(header, false)
        header = "Admin-Token"
        map[header] = HttpHeaderDocumentation(header, false)
        header = com.google.common.net.HttpHeaders.SEC_WEBSOCKET_PROTOCOL
        map[header] = HttpHeaderDocumentation(header, false)
        map
    }

}
