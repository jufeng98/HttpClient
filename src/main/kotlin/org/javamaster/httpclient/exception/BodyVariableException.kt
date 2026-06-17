package org.javamaster.httpclient.exception

/**
 * @author yudong
 */
class BodyVariableException(val variableName: String,val msg: String) : Exception(msg)
