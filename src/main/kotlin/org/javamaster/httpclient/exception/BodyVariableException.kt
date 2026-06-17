package org.javamaster.httpclient.exception

import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
class BodyVariableException(val variableName: String) : Exception(NlsBundle.nls("invalid.request", variableName, ""))
