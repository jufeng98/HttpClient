package org.javamaster.httpclient.psi.impl

import com.intellij.psi.impl.source.tree.FileElement
import org.javamaster.httpclient.psi.HttpMyJsonValue
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.psi.MyHttpTypes

/**
 * @author yudong
 */
class MyJsonLazyFileElement(val buffer: CharSequence) : FileElement(MyHttpTypes.MY_JSON_FILE, buffer) {

    companion object {
        fun parse(value: String): HttpMyJsonValue {
            val fileElement = MyJsonLazyFileElement(value)
            return HttpTypes.Factory.createElement(fileElement.firstChildNode) as HttpMyJsonValue
        }
    }

}
