package org.javamaster.httpclient

/**
 * @author yudong
 */
data class HttpInfo(
    val httpReqDescList: MutableList<String>,
    val httpResDescList: MutableList<String>,
    val type: String?,
    val byteArray: ByteArray?,
    val httpException: Exception?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpInfo

        if (httpReqDescList != other.httpReqDescList) return false
        if (httpResDescList != other.httpResDescList) return false
        if (type != other.type) return false
        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = httpReqDescList.hashCode()
        result = 31 * result + httpResDescList.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}
