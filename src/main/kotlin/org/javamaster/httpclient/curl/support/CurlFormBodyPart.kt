package org.javamaster.httpclient.curl.support

import com.intellij.openapi.util.text.StringUtil
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.FormBodyPartBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import java.io.File
import java.util.stream.Collectors


abstract class CurlFormBodyPart(protected var myFieldName: String, protected var myContentType: ContentType) {
    private var myHeaders: List<CurlRequest.KeyValuePair> = mutableListOf()

    fun addHeader(
        @Suppress("UNUSED_PARAMETER") name: String,
        @Suppress("UNUSED_PARAMETER") value: String,
    ): CurlFormBodyPart {
        return this
    }

    protected fun fillHeaders(builder: FormBodyPartBuilder): FormBodyPartBuilder {
        for (header in myHeaders) {
            builder.addField(header.key, header.value)
        }

        return builder
    }

    abstract fun toBodyPart(): FormBodyPart

    open fun toPsiRepresentation(): String {
        return myHeaders.stream()
            .map { field: CurlRequest.KeyValuePair -> field.key + ": " + field.value }
            .collect(Collectors.joining("\n"))
    }

    private class CurlStringBodyPart(fieldName: String, private val myContent: String, contentType: ContentType) :
        CurlFormBodyPart(fieldName, contentType) {
        override fun toBodyPart(): FormBodyPart {
            val builder = FormBodyPartBuilder.create(myFieldName, StringBody(myContent, myContentType))

            return fillHeaders(builder).build()
        }

        override fun toPsiRepresentation(): String {
            return """
                ${super.toPsiRepresentation()}
                
                ${StringUtil.convertLineSeparators(myContent)}
                """.trimIndent()
        }
    }

    private class CurlFileBodyPart(
        name: String,
        private val myFileName: String,
        private val myFile: File,
        contentType: ContentType,
    ) :
        CurlFormBodyPart(name, contentType) {
        override fun toBodyPart(): FormBodyPart {
            val builder = FormBodyPartBuilder.create(myFieldName, FileBody(myFile, myContentType, myFileName))

            return fillHeaders(builder).build()
        }

        override fun toPsiRepresentation(): String {
            return """
                ${super.toPsiRepresentation()}
                
                < ${myFile.path}
                """.trimIndent()
        }
    }

    companion object {
        fun create(fieldName: String, file: File): CurlFormBodyPart {
            return CurlFileBodyPart(fieldName, file.name, file, ContentType.DEFAULT_BINARY)
        }

        fun create(fieldName: String, fileName: String, file: File, contentType: ContentType): CurlFormBodyPart {
            return CurlFileBodyPart(fieldName, fileName, file, contentType)
        }

        fun create(fieldName: String, content: String, contentType: ContentType): CurlFormBodyPart {
            return CurlStringBodyPart(fieldName, content, contentType)
        }
    }
}
