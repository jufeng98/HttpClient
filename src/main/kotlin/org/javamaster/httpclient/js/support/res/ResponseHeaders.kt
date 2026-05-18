package org.javamaster.httpclient.js.support.res

/**
 * @author yudong
 */
@Suppress("unused")
class ResponseHeaders(private val headers: MutableMap<String, MutableList<String>>) {

    fun valueOf(name: String): String? {
        val list = valuesOf(name)
        if (list.isEmpty()) {
            return null
        }

        return list[0]
    }

    fun valuesOf(name: String): List<String> {
        val list = headers[name]
        if (list == null) {
            return listOf()
        }

        return list
    }

}