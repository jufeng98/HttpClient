package org.javamaster.httpclient.parser

import com.intellij.lexer.FlexAdapter
import org.javamaster.httpclient._HttpLexer

/**
 * @author yudong
 */
class HttpAdapter : FlexAdapter(_HttpLexer(null))
