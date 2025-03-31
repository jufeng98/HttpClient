package org.javamaster.httpclient.enums

import org.javamaster.httpclient.HttpIcons
import javax.swing.Icon

enum class HttpMethod(val icon: Icon) {
    UNKNOWN(HttpIcons.FILE),
    REQUEST(HttpIcons.FILE),
    GET(HttpIcons.GET),
    OPTIONS(HttpIcons.FILE),
    POST(HttpIcons.POST),
    PUT(HttpIcons.PUT),
    DELETE(HttpIcons.DELETE),
    PATCH(HttpIcons.FILE),
    HEAD(HttpIcons.FILE),
    TRACE(HttpIcons.FILE);

    companion object {
        fun parse(method: Any): HttpMethod {
            try {
                if (method is HttpMethod) {
                    return method
                }

                return valueOf(method.toString())
            } catch (ignore: Exception) {
                return REQUEST
            }
        }
    }
}
