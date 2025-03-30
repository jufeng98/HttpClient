package org.javamaster.httpclient.psi

/**
 * @author yudong
 */
interface MyHttpTypes {
    companion object {
        @JvmField
        val TEXT_VARIABLE_FILE = TextVariableILazyParseableElementType("TEXT_VARIABLE_FILE")

        @JvmField
        val URL_ENCODED_FILE = UrlEncodedLazyParseableElementType("URL_ENCODED_FILE")
    }
}
