package org.javamaster.httpclient.js.support.res

/**
 * @author yudong
 */
class ContentType(val mimeType: String, val charset: String) {

    override fun toString(): String {
        return if (charset == "") {
            mimeType
        } else {
            "$mimeType;charset=$charset"
        }
    }

}
