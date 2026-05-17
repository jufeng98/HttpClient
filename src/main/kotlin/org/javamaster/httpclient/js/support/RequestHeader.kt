package org.javamaster.httpclient.js.support

@Suppress("unused")
class RequestHeader(val name: String, val values: List<String>) {

    fun getRawValue(): String {
        return values[0]
    }

    fun tryGetSubstitutedValue(): String {
        return values[0]
    }

    fun value(): String {
        return values[0]
    }

}