package org.javamaster.httpclient.enums


enum class SimpleTypeEnum(val type: String) {
    JSON("json"),
    HTML("html"),
    XML("xml"),
    TXT("txt"),
    TEXT("text"),

    IMAGE(""),
    ;

    companion object {
        fun isImage(contentType: String): Boolean {
            return contentType.contains("jpg") || contentType.contains("jpeg") || contentType.contains("png")
        }
    }
}
