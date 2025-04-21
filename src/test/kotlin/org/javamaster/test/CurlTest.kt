package org.javamaster.test

import org.javamaster.httpclient.curl.CurlParser
import org.junit.Test

class CurlTest {

    @Test
    fun testParse() {
        val curl = "curl -d \"birthyear=1905&press=OK\" www.hotmail.com/when/junk.cgi"
        val request = CurlParser(curl).parseToCurlRequest()
        println(request)
    }

    @Test
    fun testSerialize() {

    }
}
