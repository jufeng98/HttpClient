package org.javamaster.httpclient.manipulator

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulator
import org.javamaster.httpclient.psi.HttpFilePath

class HttpFilePathManipulator : ElementManipulator<HttpFilePath> {
    override fun handleContentChange(element: HttpFilePath, range: TextRange, newContent: String?): HttpFilePath {
        return element
    }

    override fun handleContentChange(element: HttpFilePath, newContent: String?): HttpFilePath {
        return element
    }

    override fun getRangeInElement(element: HttpFilePath): TextRange {
        return element.textRange
    }
}
