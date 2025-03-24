package org.javamaster.httpclient.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariable
import org.javamaster.httpclient.reference.support.JsonValueVariablePsiReference

/**
 * @author yudong
 */
class JsonValueVariableAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val references = element.references
        if (references.isEmpty()) {
            return
        }

        references.forEach {
            if (it !is JsonValueVariablePsiReference) {
                return@forEach
            }

            val variableName = it.variableName

            val range = it.textRange.shiftRight(element.startOffset)

            annotateVariable(variableName.startsWith("$"), range, holder)
        }
    }

}
