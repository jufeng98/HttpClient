package org.javamaster.httpclient.exception

import java.net.URISyntaxException

/**
 * @author yudong
 */
class UrlVariableException(val url: String, cause: URISyntaxException) : Exception(cause)