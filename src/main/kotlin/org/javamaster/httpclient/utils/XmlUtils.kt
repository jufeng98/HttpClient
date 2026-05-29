package org.javamaster.httpclient.utils

import javax.xml.parsers.DocumentBuilderFactory

object XmlUtils {

    @Suppress("HttpUrlsUsage")
    val documentBuilderFactory: DocumentBuilderFactory by lazy {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        factory
    }

}
