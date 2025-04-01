package org.javamaster.httpclient.enums

import org.javamaster.httpclient.HttpIcons
import javax.swing.Icon

enum class HttpMethod(val icon: Icon) {
    GET(HttpIcons.GET),
    POST(HttpIcons.POST),
    PUT(HttpIcons.PUT),
    DELETE(HttpIcons.DELETE),
    OPTIONS(HttpIcons.FILE),
    PATCH(HttpIcons.FILE),
    HEAD(HttpIcons.FILE),
    TRACE(HttpIcons.FILE),
    REQUEST(HttpIcons.FILE),
    UNKNOWN(HttpIcons.FILE);

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

        fun getMethods(): List<HttpMethod> {
            return entries
                .filter { it != UNKNOWN && it != REQUEST }
                .toList()
        }
    }
}
