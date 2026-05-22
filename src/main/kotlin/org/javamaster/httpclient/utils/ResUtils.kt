package org.javamaster.httpclient.utils

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.HttpStatus
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.consts.HttpConsts.Companion.RES_SIZE_LIMIT
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.model.HttpResInfo
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.JsonUtils.formatJson
import java.net.http.HttpHeaders
import java.nio.charset.StandardCharsets
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.jvm.optionals.getOrElse

object ResUtils {

    fun convertResponseHeaders(headers: HttpHeaders): MutableList<String> {
        val headerDescList = mutableListOf<String>()

        headers.map()
            .forEach { (t, u) ->
                u.forEach {
                    headerDescList.add("$t: $it$CR_LF")
                }
            }

        headerDescList.add(CR_LF)

        return headerDescList
    }

    fun convertResponseBody(resBody: ByteArray, resHeaders: HttpHeaders): HttpResInfo {
        var bodyBytes = resBody
        val contentType = resHeaders.firstValue(CONTENT_TYPE).getOrElse { ContentType.TEXT_PLAIN.mimeType }

        val simpleTypeEnum = SimpleTypeEnum.convertContentType(contentType)

        val bodyStr = if (simpleTypeEnum.binary) {
            null
        } else {
            val str = String(bodyBytes, StandardCharsets.UTF_8)

            if (simpleTypeEnum == SimpleTypeEnum.JSON) {
                if (bodyBytes.size > RES_SIZE_LIMIT) {
                    str
                } else {
                    val prettyStr = formatJson(str)
                    if (prettyStr.length > RES_SIZE_LIMIT) {
                        str
                    } else {
                        bodyBytes = prettyStr.toByteArray(StandardCharsets.UTF_8)

                        prettyStr
                    }
                }
            } else {
                str
            }
        }

        return HttpResInfo(simpleTypeEnum, bodyBytes, bodyStr, contentType)
    }

    fun shouldRedirect(httpStatus: Int?, paramMap: Map<String, String>): Boolean {
        httpStatus ?: return false

        if (httpStatus != HttpStatus.SC_MOVED_TEMPORARILY && httpStatus != HttpStatus.SC_MOVED_PERMANENTLY) {
            return false
        }

        return paramMap.contains(ParamEnum.AUTO_REDIRECT.param)
    }

    fun resolveLocationUrl(url: String, headers: HttpHeaders): String {
        val location = headers.firstValue(com.google.common.net.HttpHeaders.LOCATION).get()
        if (location.startsWith("http")) {
            return location
        }

        val httpUrl = url.toHttpUrl()
        val scheme = httpUrl.scheme
        val host = httpUrl.host
        val port = httpUrl.port
        val portStr = if (port == 443 || port == 80) "" else ":$port"
        return "$scheme://$host$portStr$location"
    }

}