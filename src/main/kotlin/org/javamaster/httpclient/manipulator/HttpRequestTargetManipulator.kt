package org.javamaster.httpclient.manipulator

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import org.javamaster.httpclient.psi.HttpRequestTarget

class HttpRequestTargetManipulator : AbstractElementManipulator<HttpRequestTarget>() {

    override fun handleContentChange(p0: HttpRequestTarget, p1: TextRange, p2: String?): HttpRequestTarget {
        return p0
    }

}
