package org.javamaster.httpclient.psi

import com.intellij.psi.tree.IElementType
import org.javamaster.httpclient.CookieLanguage
import org.jetbrains.annotations.NonNls

/**
 * @author yudong
 */
class CookieElementType(debugName: @NonNls String) : IElementType(debugName, CookieLanguage.INSTANCE)
