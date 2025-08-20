package org.javamaster.httpclient.curl.support

import com.intellij.openapi.util.text.StringUtil
import org.apache.http.cookie.Cookie
import org.javamaster.httpclient.curl.data.CurlAuthData
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

    @Suppress("unused")
    fun getHeaderValue(name: String, defaultValue: String?): String? {
        for (header in headers) {
            if (name == header.key) {
                return header.value
            }
        }

        return defaultValue
    }

    @Suppress("unused")
    fun getHeadersValue(name: String): List<String> {
        val list = mutableListOf<String>()

        for (header in headers) {
            if (name == header.key) {
                list.add(header.value)
            }
        }

        return if (list.isEmpty()) emptyList() else list
    }

    @Suppress("unused")
    fun deleteHeader(name: String) {
        headers.removeIf { name == it.key }
    }

    val files by lazy {
        val files = mutableListOf<File>()

        for (path in StringUtil.split(filesToSend!!, File.pathSeparator)) {
            files.add(File(path))
        }

        files
    }

    private val url by lazy {
        var base = urlBase!!

        if (!base.endsWith("/") && urlPath!!.isNotEmpty()) {
            base = "$base/"
        }

        base = if (urlPath!!.startsWith("/")) base + urlPath!!.substring(1) else base + urlPath

        base.replace(" ", "%20")
    }

    private fun createQueryString(): String {
        return StringUtil.join(
            parameters,
            {
                try {
                    val key = URLEncoder.encode(it.key, "UTF-8")
                    val value = URLEncoder.encode(it.value, "UTF-8")
                    "$key=$value"
                } catch (_: UnsupportedEncodingException) {
                    ""
                }
            }, "&"
        )
    }

    @Suppress("unused")
    fun addBiscuit(cookie: Cookie) {
        val date = cookie.expiryDate

        biscuits.add(
            Biscuit(cookie.name, cookie.value, cookie.domain, cookie.path, date?.time ?: -1L)
        )
    }

    @Suppress("unused")
    val isEmptyCredentials = authData === CurlAuthData.EMPTY_CREDENTIALS

    @Suppress("unused")
    fun setEmptyCredentials() {
        authData = CurlAuthData.EMPTY_CREDENTIALS
    }

    override fun toString(): String {
        val queryString = createQueryString()

        return if (queryString.isNotEmpty()) url + (if (url.contains("?")) "&" else "?") + queryString else url
    }

    class KeyValuePair(var key: String, var value: String)

    class Biscuit(var name: String?, var value: String?, @Suppress("unused") var domain: String?, var path: String?, var date: Long)
}
