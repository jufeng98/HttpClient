package org.javamaster.httpclient.manipulator

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import org.javamaster.httpclient.psi.HttpRequestTarget

class HttpRequestTargetManipulator : AbstractElementManipulator<HttpRequestTarget>() {

    override fun handleContentChange(
        element: HttpRequestTarget,
        range: TextRange,
        newContent: String,
    ): HttpRequestTarget {
        // 不会有重命名,无需实现
        return element
    }

}
