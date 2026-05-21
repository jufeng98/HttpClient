package org.javamaster.httpclient.js.support.req

/**
 * @author yudong
 */
@Suppress("unused")
class RequestUrl(private val url: String, private val rawUrl: String) {

    fun getRaw(): String {
        return rawUrl
    }

    fun tryGetSubstituted(): String {
        return url
    }

    override fun toString(): String {
        return "RequestUrl(url='$url', rawUrl='$rawUrl')"
    }

}
