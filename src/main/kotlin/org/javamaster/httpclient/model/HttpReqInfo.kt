package org.javamaster.httpclient.model

/**
 * reqBody type could be String, Pair<ByteArray, String>, MutableList<Pair<ByteArray, String>>
 *
 * @author yudong
 */
data class HttpReqInfo(val reqBody: Any?, val environment: MutableMap<String, String>, val preJsFiles: List<PreJsFile>)
