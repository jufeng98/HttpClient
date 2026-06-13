package org.javamaster.httpclient.exception

/**
 * @author yudong
 */
class JsScriptException(message: String, val list: List<String>, val before: Boolean, cause: Exception?) :
    Exception(message, cause)
