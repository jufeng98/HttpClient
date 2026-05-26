package org.javamaster.httpclient.utils

import org.intellij.markdown.html.urlEncode
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import java.net.http.HttpRequest.BodyPublishers

/**
 * @author yudong
 */
class ReqUtils {

    companion object {

        fun convertToReqBodyPublisher(reqBody: Any?): Pair<java.net.http.HttpRequest.BodyPublisher, Long> {
            if (reqBody == null) {
                return Pair(BodyPublishers.noBody(), 0L)
            }

            var multipartLength = 0L
            val bodyPublisher: java.net.http.HttpRequest.BodyPublisher

            when (reqBody) {
                is String -> {
                    bodyPublisher = BodyPublishers.ofString(reqBody)
                }

                is Pair<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val pair = reqBody as Pair<ByteArray, String>

                    bodyPublisher = BodyPublishers.ofByteArray(pair.first)
                }

                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val list = reqBody as MutableList<Pair<ByteArray, String>>

                    val byteArrays = list.map { it.first }

                    bodyPublisher = BodyPublishers.ofByteArrays(byteArrays)

                    multipartLength = byteArrays.sumOf { it.size.toLong() }
                }

                else -> {
                    System.err.println(NlsBundle.nls("reqBody.unknown", reqBody.javaClass))

                    bodyPublisher = BodyPublishers.noBody()
                }
            }

            return Pair(bodyPublisher, multipartLength)
        }

        fun convertReqBody(reqBody: Any?): Any? {
            if (reqBody == null) {
                return null
            }

            if (reqBody is String) {
                return reqBody
            }

            return when (reqBody) {
                is Pair<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val pair = reqBody as Pair<ByteArray, String>

                    pair.first
                }

                is MutableList<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val list = reqBody as MutableList<Pair<ByteArray, String>>

                    list.map { it.first }.reduce { a, b -> a + b }
                }

                else -> {
                    throw IllegalArgumentException(NlsBundle.nls("reqBody.unknown", reqBody.javaClass))
                }
            }
        }

        fun resolveReqBodyAgain(reqBody: Any?, resolver: VariableResolver): Any? {
            if (reqBody == null) {
                return null
            }

            if (reqBody is String) {
                return resolver.resolve(reqBody)
            }

            return reqBody
        }

        fun getReqBodyDesc(reqBody: Any?): MutableList<String> {
            val maxSizeLimit = 50000
            val descList = mutableListOf<String>()

            when (reqBody) {
                is String -> {
                    if (reqBody.length > maxSizeLimit) {
                        descList.add(
                            reqBody.substring(0, maxSizeLimit) + "$CR_LF......(${NlsBundle.nls("content.truncated")})"
                        )
                    } else {
                        descList.add(reqBody)
                    }
                }

                is Pair<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val pair = reqBody as Pair<ByteArray, String>

                    descList.add(pair.second)
                }

                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val list = reqBody as MutableList<Pair<ByteArray, String>>

                    list.forEach {
                        val desc = it.second

                        val bodyDesc = if (desc.length > maxSizeLimit) {
                            desc + "$CR_LF......(${NlsBundle.nls("content.truncated")})$CR_LF"
                        } else {
                            desc
                        }

                        descList.add(bodyDesc)
                    }
                }
            }

            return descList
        }

        fun handleUrl(url: String): String {
            val split = url.split("?")
            if (split.size == 1) {
                return url
            }

            return split[0] + "?" + handleQueryParam(split[1])
        }

        fun encodeUrl(url: String): String {
            val split = url.split("?")
            if (split.size == 1) {
                return url
            }

            return split[0] + "?" + encodeQueryParam(split[1])
        }

        fun handleQueryParam(queryParam: String): String {
            val split = queryParam.split("&")

            return split.joinToString("&") {
                val list = it.split("=")
                if (list.size == 1 && list[0].startsWith("{{")) {
                    return@joinToString list[0]
                }

                list[0].trim() + "=" + list[1].trim()
            }
        }

        fun encodeQueryParam(queryParam: String): String {
            val split = queryParam.split("&")

            return split.joinToString("&") {
                val list = it.split("=")
                urlEncode(list[0]) + "=" + urlEncode(list[1])
            }
        }

    }
}
