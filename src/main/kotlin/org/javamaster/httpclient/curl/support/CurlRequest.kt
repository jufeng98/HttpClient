package org.javamaster.httpclient.curl.support

import com.intellij.openapi.util.text.StringUtil
import org.apache.http.cookie.Cookie
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class CurlRequest {
    private var biscuits: MutableList<Biscuit> = mutableListOf()
    var httpMethod: String? = null
    var urlBase: String? = null
    var urlPath: String? = null
    var headers: MutableList<KeyValuePair> = mutableListOf()
    var parameters: List<KeyValuePair> = mutableListOf()
    var haveTextToSend: Boolean = false
    var haveFileToSend: Boolean = false
    var isFileUpload: Boolean = false
    var textToSend: String? = null
    var filesToSend: String? = null

    var formBodyPart: MutableList<CurlFormBodyPart> = mutableListOf()

    var multipartBoundary: String? = null

    var authData: CurlAuthData? = null

    fun getHeaderValue(name: String, defaultValue: String?): String? {
        for (header in headers) {
            if (name == header.key) {
                return header.value
            }
        }

        return defaultValue
    }

    fun getHeadersValue(name: String): List<String> {
        val list: MutableList<String> = ArrayList()

        for (header in headers) {
            if (name == header.key) {
                list.add(header.value)
            }
        }

        return if (list.isEmpty()) emptyList() else list
    }

    fun deleteHeader(name: String) {
        headers.removeIf { header: KeyValuePair? -> name == header!!.key }
    }

    val files: List<File>
        get() {
            val files: MutableList<File> = ArrayList()

            for (path in StringUtil.split(
                filesToSend!!,
                File.pathSeparator
            )) {
                files.add(File(path))
            }

            return files
        }

    private val url: String
        get() {
            var base = urlBase
            if (!base!!.endsWith("/") && urlPath!!.isNotEmpty()) {
                base = "$base/"
            }

            base =
                if (urlPath!!.startsWith("/")) base + urlPath!!.substring(1) else base + urlPath
            return base.replace(" ", "%20")
        }

    private fun createQueryString(): String {
        return StringUtil.join(
            parameters,
            { pair: KeyValuePair? ->
                try {
                    val key = URLEncoder.encode(pair!!.key, "UTF-8")
                    val value = URLEncoder.encode(pair.value, "UTF-8")
                    return@join "$key=$value"
                } catch (var3: UnsupportedEncodingException) {
                    return@join ""
                }
            }, "&"
        )
    }

    fun addBiscuit(cookie: Cookie) {
        val date = cookie.expiryDate
        biscuits.add(
            Biscuit(
                cookie.name, cookie.value, cookie.domain, cookie.path,
                date?.time ?: -1L
            )
        )
    }

    val isEmptyCredentials: Boolean
        get() = authData === CurlAuthData.EMPTY_CREDENTIALS

    fun setEmptyCredentials() {
        authData = CurlAuthData.EMPTY_CREDENTIALS
    }

    override fun toString(): String {
        val queryString = createQueryString()
        return if (queryString.isNotEmpty()) url + (if (url.contains("?")) "&" else "?") + queryString else url
    }

    class KeyValuePair(var key: String, var value: String)

    class Biscuit(var name: String?, var value: String?, var domain: String?, var path: String?, var date: Long)
}
