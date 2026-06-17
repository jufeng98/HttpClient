package org.javamaster.httpclient.exception

import java.net.URISyntaxException

/**
 * @author yudong
 */
class UrlUnresolvedVariableException(val url: String, cause: URISyntaxException) : Exception(cause)