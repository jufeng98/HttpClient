package org.javamaster.httpclient.utils

import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

object XmlUtils {

    @Suppress("HttpUrlsUsage")
    private val documentBuilderFactory: DocumentBuilderFactory by lazy {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        factory
    }

    fun parseXml(xmlStr: String): Document {
        return documentBuilderFactory.newDocumentBuilder().parse(InputSource(StringReader(xmlStr)))
    }

}
