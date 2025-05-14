package org.javamaster.httpclient.curl.data

import org.javamaster.httpclient.curl.support.CurlRequest

interface CurlDataOption {
    fun apply(curlRequest: CurlRequest)
}
