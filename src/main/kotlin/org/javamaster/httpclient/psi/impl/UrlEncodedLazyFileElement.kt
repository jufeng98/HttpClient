package org.javamaster.httpclient.psi.impl

import com.intellij.psi.impl.source.tree.FileElement
import org.javamaster.httpclient.psi.HttpQuery
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.psi.MyHttpTypes

/**
 * @author yudong
 */
class UrlEncodedLazyFileElement(val buffer: CharSequence) : FileElement(MyHttpTypes.URL_ENCODED_FILE, buffer) {

    companion object {
        fun parse(value: String): HttpQuery {
            val fileElement = UrlEncodedLazyFileElement(value)
            return HttpTypes.Factory.createElement(fileElement.firstChildNode.firstChildNode) as HttpQuery
        }
    }

}
