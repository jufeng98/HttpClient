package org.javamaster.httpclient.exception

import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
class HeaderUnresolvedVariableException(val variableName: String) :
    Exception(NlsBundle.nls("invalid.request", variableName, ""))
