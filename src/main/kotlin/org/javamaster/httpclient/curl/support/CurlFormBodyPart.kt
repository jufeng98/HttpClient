package org.javamaster.httpclient.curl.support

import com.intellij.openapi.util.text.StringUtil
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.FormBodyPartBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import java.io.File
import java.util.stream.Collectors


abstract class CurlFormBodyPart(var myFieldName: String) {
    private var myHeaders: MutableList<CurlRequest.KeyValuePair> = mutableListOf()

    fun addHeader(name: String, value: String): CurlFormBodyPart {
        myHeaders.add(CurlRequest.KeyValuePair(name, value))

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
            .map { it.key + ": " + it.value }
            .collect(Collectors.joining("\n"))
    }

    private class CurlStringBodyPart(
        fieldName: String,
        private val myContent: String,
        private val contentType: ContentType,
    ) :
        CurlFormBodyPart(fieldName) {

        override fun toBodyPart(): FormBodyPart {
            val builder = FormBodyPartBuilder.create(myFieldName, StringBody(myContent, contentType))

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
        private val contentType: ContentType,
    ) :
        CurlFormBodyPart(name) {

        override fun toBodyPart(): FormBodyPart {
            val builder = FormBodyPartBuilder.create(myFieldName, FileBody(myFile, contentType, myFileName))

            return fillHeaders(builder).build()
        }

        override fun toPsiRepresentation(): String {
            return """
                ${super.toPsiRepresentation()}
                
                < ${myFile.absolutePath}
                """.trimIndent()
        }
    }

    companion object {
        fun create(fieldName: String, fileName: String, file: File, contentType: ContentType): CurlFormBodyPart {
            return CurlFileBodyPart(fieldName, fileName, file, contentType)
        }

        fun create(fieldName: String, content: String, contentType: ContentType): CurlFormBodyPart {
            return CurlStringBodyPart(fieldName, content, contentType)
        }
    }
}
