package org.javamaster.httpclient.completion.support

import com.google.common.collect.Maps
import org.apache.http.HttpHeaders
import org.apache.http.entity.ContentType

object HttpHeadersDictionary {

    @JvmStatic
    fun getHeaderValues(headerName: String): Array<String> {
        if (headerName == HttpHeaders.CONTENT_TYPE || headerName == HttpHeaders.ACCEPT) {
            val field = ContentType::class.java.getDeclaredField("CONTENT_TYPE_MAP")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val map = field.get(null) as Map<String, ContentType>
            return map.keys.toTypedArray()
        }

        return arrayOf()
    }

    private var map: MutableMap<String, HttpHeaderDocumentation> = Maps.newHashMap()

    init {
        val fields = HttpHeaders::class.java.declaredFields
        for (field in fields) {
            field.isAccessible = true
            try {
                val value = field[null] as String
                map[value] = HttpHeaderDocumentation(value, false)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        val header = "Admin-Token"
        map[header] = HttpHeaderDocumentation(header, false)
    }

    @JvmStatic
    val headers: Map<String, HttpHeaderDocumentation>
        get() = map
}
