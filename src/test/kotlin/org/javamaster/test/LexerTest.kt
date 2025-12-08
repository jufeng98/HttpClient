package org.javamaster.test

import org.javamaster.httpclient.parser.HttpAdapter
import org.javamaster.utils.HttpParseUtils
import org.javamaster.httpclient.utils.StreamUtils
import org.junit.Test
import java.nio.charset.StandardCharsets

class LexerTest {

    @Test
    fun testLexer() {
        val stream = checkNotNull(javaClass.classLoader.getResourceAsStream("test.http"))
        val bytes = StreamUtils.copyToByteArray(stream)
        val str = String(bytes, StandardCharsets.UTF_8)

        val httpAdapter = HttpAdapter()
        httpAdapter.start(str)

        while (true) {
            val tokenType = httpAdapter.tokenType
            println(tokenType)

            httpAdapter.advance()
            if (tokenType == null) {
                break
            }
        }
    }

    @Test
    fun testParser() {
        val stream = checkNotNull(javaClass.classLoader.getResourceAsStream("test.http"))
        val bytes = StreamUtils.copyToByteArray(stream)
        val str = String(bytes, StandardCharsets.UTF_8)

        val psiElement = HttpParseUtils.parse(str)

        println(psiElement)

        println(psiElement.findElementAt(66))
    }
}
