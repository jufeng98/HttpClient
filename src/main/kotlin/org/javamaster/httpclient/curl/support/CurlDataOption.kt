package org.javamaster.httpclient.curl.support

interface CurlDataOption {
    fun apply(curlRequest: CurlRequest)
}
