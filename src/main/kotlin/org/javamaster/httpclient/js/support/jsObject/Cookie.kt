package org.javamaster.httpclient.js.support.jsObject

@Suppress("unused")
data class Cookie(
    val domain: String, val path: String, val name: String,
    val value: String, val expiresAt: Long, val httpOnly: Boolean,
    val secure: Boolean,
)
