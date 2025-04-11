package org.javamaster.httpclient.model

/**
 * @author yudong
 */
data class HttpReqInfo(val reqBody: Any?, val environment: String, val preJsFiles: List<PreJsFile>)
