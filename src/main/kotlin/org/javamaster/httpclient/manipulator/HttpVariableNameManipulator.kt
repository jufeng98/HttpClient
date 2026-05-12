package org.javamaster.httpclient.manipulator

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import org.javamaster.httpclient.factory.HttpPsiFactory
import org.javamaster.httpclient.psi.HttpVariableName

/**
 * @author yudong
 */
class HttpVariableNameManipulator : AbstractElementManipulator<HttpVariableName>() {

    override fun handleContentChange(
        element: HttpVariableName,
        range: TextRange,
        newContent: String,
    ): HttpVariableName {
        val variable = HttpPsiFactory.createVariable(element.project, "GET {{$newContent}}")
        val newVariableName = variable.variableName!!
        return element.replace(newVariableName) as HttpVariableName
    }

}
