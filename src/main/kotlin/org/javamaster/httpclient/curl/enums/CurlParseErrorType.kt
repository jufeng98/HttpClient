package org.javamaster.httpclient.curl.enums


enum class CurlParseErrorType {
    NOT_CURL,
    NO_URL,
    INCOMPLETE_OPTION,
    UNKNOWN_OPTION,
    INVALID_HTTP_METHOD,
    INVALID_FORM_DATA,
    INVALID_HEADER,
    INVALID_URL
}