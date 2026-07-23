package org.javamaster.httpclient.utils

import com.intellij.json.psi.JsonBooleanLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral

object JsonUtil {

    fun getBoolValue(sslObj: JsonObject?, propertyName: String, def: Boolean): Boolean {
        if (sslObj == null) {
            return def
        }

        val value = sslObj.findProperty(propertyName)?.value
        if (value == null) {
            return def
        }

        if (value !is JsonBooleanLiteral) {
            return def
        }

        return value.value
    }

    fun getStrValue(sslObj: JsonObject?, propertyName: String): String? {
        if (sslObj == null) {
            return null
        }

        val value = sslObj.findProperty(propertyName)?.value
        if (value == null) {
            return null
        }

        if (value !is JsonStringLiteral) {
            return null
        }

        return value.value
    }

}
