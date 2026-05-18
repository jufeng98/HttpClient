package org.javamaster.httpclient.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import org.javamaster.httpclient.adapter.DateTypeAdapter
import java.util.*

/**
 * @author yudong
 */
object JsonUtils {
    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .disableHtmlEscaping()
        .registerTypeAdapter(Date::class.java, DateTypeAdapter)
        .create()

    val gsonNotPretty: Gson = GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .registerTypeAdapter(Date::class.java, DateTypeAdapter)
        .create()

    fun formatJson(jsonStr: String): String {
        try {
            val jsonElement = gson.fromJson(jsonStr, JsonElement::class.java)

            return gson.toJson(jsonElement)
        } catch (_: JsonSyntaxException) {
            return jsonStr
        }
    }
}
