package org.javamaster.httpclient.model

import org.javamaster.httpclient.enums.SimpleTypeEnum

data class HttpResInfo(
    val simpleTypeEnum: SimpleTypeEnum,
    val bodyBytes: ByteArray,
    val bodyStr: String?,
    val contentType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResInfo

        if (simpleTypeEnum != other.simpleTypeEnum) return false
        if (!bodyBytes.contentEquals(other.bodyBytes)) return false
        if (bodyStr != other.bodyStr) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = simpleTypeEnum.hashCode()
        result = 31 * result + bodyBytes.contentHashCode()
        result = 31 * result + (bodyStr?.hashCode() ?: 0)
        result = 31 * result + contentType.hashCode()
        return result
    }
}
