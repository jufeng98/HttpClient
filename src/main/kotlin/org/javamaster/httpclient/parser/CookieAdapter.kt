package org.javamaster.httpclient.parser

import com.intellij.lexer.FlexAdapter
import org.javamaster.httpclient._CookieLexer

/**
 * @author yudong
 */
open class CookieAdapter : FlexAdapter(_CookieLexer(null))
