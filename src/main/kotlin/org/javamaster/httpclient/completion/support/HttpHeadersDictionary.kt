package org.javamaster.httpclient.completion.support

import com.google.common.net.HttpHeaders
import com.google.common.net.HttpHeaders.ReferrerPolicyValues
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.utils.DubboUtils

/**
 * @author yudong
 */
object HttpHeadersDictionary {

    val contentTypeValues by lazy {
        val field = ContentType::class.java.getDeclaredField("CONTENT_TYPE_MAP")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val map = field.get(null) as Map<String, ContentType>

        val list = mutableListOf(ContentType.MULTIPART_FORM_DATA.mimeType + "; boundary=----WebBoundary")
        list.addAll(map.keys)

        list
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

    val secWebsocketProtocolValues by lazy {
        listOf("graphql-ws", "subscriptions-transport-ws", "aws-app-sync")
    }

    val headerNameMap by lazy {
        val map: MutableMap<String, HttpHeaderDocumentation> = mutableMapOf()
        val fields = HttpHeaders::class.java.declaredFields
        for (field in fields) {
            field.isAccessible = true
            val value = field[null] as String

            val isDeprecated = field.getAnnotation(java.lang.Deprecated::class.java) != null
            map[value] = HttpHeaderDocumentation(value, isDeprecated)
        }

        val header = "Admin-Token"
        map[header] = HttpHeaderDocumentation(header, false)

        map
    }

    val referrerPolicyValues by lazy {
        val fields = ReferrerPolicyValues::class.java.declaredFields
        fields.map {
            it.isAccessible = true
            it[null] as String
        }
    }
}
