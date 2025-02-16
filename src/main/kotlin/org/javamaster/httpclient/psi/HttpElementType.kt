package org.javamaster.httpclient.psi

import com.intellij.psi.tree.IElementType
import org.javamaster.httpclient.HttpLanguage
import org.jetbrains.annotations.NonNls

/**
 * @author yudong
 */
class HttpElementType(debugName: @NonNls String) : IElementType(debugName, HttpLanguage.INSTANCE) {
    constructor(debugName: String, text: String) : this(debugName)

    val name: String
        get() = debugName
}
