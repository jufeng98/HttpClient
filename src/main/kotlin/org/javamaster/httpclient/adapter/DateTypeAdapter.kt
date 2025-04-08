package org.javamaster.httpclient.adapter

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.apache.commons.lang3.time.FastDateFormat
import java.util.*

/**
 * @author yudong
 */
object DateTypeAdapter : TypeAdapter<Date?>() {
    private val dateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE)

    override fun write(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val dateFormatAsString = dateFormat.format(value)
            writer.value(dateFormatAsString)
        }
    }

    override fun read(reader: JsonReader): Date? {
        when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                return null
            }

            else -> {
                val date = reader.nextString()
                return dateFormat.parse(date)
            }
        }
    }
}
