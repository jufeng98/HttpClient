package org.javamaster.httpclient.manipulator

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import org.javamaster.httpclient.psi.HttpFilePath

class HttpFilePathManipulator : AbstractElementManipulator<HttpFilePath>() {

    override fun handleContentChange(element: HttpFilePath, range: TextRange, newContent: String): HttpFilePath {
        // 不会有重命名,无需实现
        return element
    }

}
