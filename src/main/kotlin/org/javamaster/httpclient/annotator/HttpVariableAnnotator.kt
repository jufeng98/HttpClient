package org.javamaster.httpclient.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariable
import org.javamaster.httpclient.psi.HttpVariable
import org.javamaster.httpclient.reference.support.HttpVariablePsiReference

/**
 * @author yudong
 */
class HttpVariableAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is HttpVariable) return

        val references = element.references
        if (references.isEmpty()) {
            return
        }

        val reference = references[0]
        if (reference !is HttpVariablePsiReference) {
            return
        }

        val range = reference.textRange.shiftRight(element.startOffset)

        annotateVariable(element.isBuiltin, range, holder)
    }

}
