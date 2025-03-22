package org.javamaster.httpclient.enums


enum class SimpleTypeEnum(val type: String) {
    JSON("json"),
    HTML("html"),
    XML("xml"),
    TXT("txt"),
    TEXT("text"),

    STREAM("stream"),
    IMAGE("image"),
    ;
}
