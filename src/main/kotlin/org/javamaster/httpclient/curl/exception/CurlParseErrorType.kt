package org.javamaster.httpclient.curl.exception


enum class CurlParseErrorType {
    NOT_CURL,
    NO_URL,
    INCOMPLETE_OPTION,
    UNKNOWN_OPTION,
    INVALID_HTTP_METHOD,
    INVALID_FORM_DATA,
    INVALID_HEADER,
    UNSUPPORTED_ENCODING,
    INVALID_URL
}