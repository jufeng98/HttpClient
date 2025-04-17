package org.javamaster.httpclient

import org.javamaster.httpclient.enums.SimpleTypeEnum

/**
 * @author yudong
 */
data class HttpInfo(
    val httpReqDescList: MutableList<String>,
    val httpResDescList: MutableList<String>,
    val type: SimpleTypeEnum?,
    val byteArray: ByteArray?,
    val httpException: Throwable?,
    var contentType: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpInfo

        if (httpReqDescList != other.httpReqDescList) return false
        if (httpResDescList != other.httpResDescList) return false
        if (type != other.type) return false
        if (byteArray != null) {
            if (other.byteArray == null) return false
            if (!byteArray.contentEquals(other.byteArray)) return false
        } else if (other.byteArray != null) return false
        if (httpException != other.httpException) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = httpReqDescList.hashCode()
        result = 31 * result + httpResDescList.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (byteArray?.contentHashCode() ?: 0)
        result = 31 * result + (httpException?.hashCode() ?: 0)
        result = 31 * result + (contentType?.hashCode() ?: 0)
        return result
    }

}
