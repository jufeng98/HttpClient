package org.javamaster.httpclient.psi

import com.intellij.psi.tree.IElementType
import org.javamaster.httpclient.HttpLanguage
import org.jetbrains.annotations.NonNls

/**
 * @author yudong
 */
class HttpTokenType(debugName: @NonNls String) : IElementType(debugName, HttpLanguage.INSTANCE) {
    override fun toString(): String {
        return HttpTokenType::class.java.simpleName + "." + super.toString()
    }
}
