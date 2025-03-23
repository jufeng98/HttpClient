package org.javamaster.httpclient.annotator

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariable
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class JsonValueVariableAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is JsonStringLiteral || element.isPropertyName) return

        val value = element.value
        if (!value.startsWith(HttpUtils.VARIABLE_SIGN_START)) return

        val textRange = element.textRange
        val range = TextRange(textRange.startOffset + 3, textRange.endOffset - 3)

        val str = value.substring(2)
        annotateVariable(str.startsWith("$"), range, holder)
    }

}
