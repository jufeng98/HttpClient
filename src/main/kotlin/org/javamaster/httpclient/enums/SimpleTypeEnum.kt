package org.javamaster.httpclient.enums


enum class SimpleTypeEnum(val type: String, val binary: Boolean) {
    JSON("json", false),
    HTML("html", false),
    XML("xml", false),
    TXT("txt", false),
    TEXT("text", false),

    STREAM("stream", true),
    IMAGE("image", true),
    PDF("pdf", true),
    EXCEL("excel", true),
    ZIP("zip", true),
    ;

    companion object {

        fun isTextContentType(contentType: String): Boolean {
            val simpleTypeEnum = SimpleTypeEnum.convertContentType(contentType)

            return !simpleTypeEnum.binary
        }

        fun getSuffix(simpleTypeEnum: SimpleTypeEnum, contentType: String): String {
            return when (simpleTypeEnum) {
                IMAGE -> {
                    contentType.split("/").last()
                }

                PDF -> "pdf"
                EXCEL -> "xls"
                ZIP -> "zip"
                else -> "bin"
            }
        }

        fun convertContentType(contentType: String): SimpleTypeEnum {
            if (contentType.contains(JSON.type)) {
                return JSON
            }

            if (contentType.contains(HTML.type)) {
                return HTML
            }

            if (contentType.contains(XML.type)) {
                return XML
            }

            if (contentType.contains(TEXT.type)) {
                return TEXT
            }

            if (contentType.contains(TXT.type)) {
                return TXT
            }

            if (contentType.contains(IMAGE.type)) {
                return IMAGE
            }

            if (contentType.contains(PDF.type)) {
                return PDF
            }

            if (contentType.contains(EXCEL.type)) {
                return EXCEL
            }

            if (contentType.contains(ZIP.type)) {
                return ZIP
            }

            return STREAM
        }

    }
}
