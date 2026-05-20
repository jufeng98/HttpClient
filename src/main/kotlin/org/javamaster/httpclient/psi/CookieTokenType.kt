package org.javamaster.httpclient.psi

import com.intellij.psi.tree.IElementType
import org.javamaster.httpclient.CookieLanguage
import org.jetbrains.annotations.NonNls

/**
 * @author yudong
 */
class CookieTokenType(debugName: @NonNls String) : IElementType(debugName, CookieLanguage.INSTANCE) {

    override fun toString(): String {
        return CookieTokenType::class.java.simpleName + "." + super.toString()
    }

}
