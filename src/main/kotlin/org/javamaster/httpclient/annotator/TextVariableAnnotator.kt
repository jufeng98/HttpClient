package org.javamaster.httpclient.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariableArg
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariableName
import org.javamaster.httpclient.reference.support.TextVariableArgNamePsiReference
import org.javamaster.httpclient.reference.support.TextVariableNamePsiReference

/**
 * @author yudong
 */
class TextVariableAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val references = element.references
        if (references.isEmpty()) {
            return
        }

        references.forEach {
            if (it is TextVariableNamePsiReference) {
                val builtin = it.variable.variableName?.isBuiltin ?: false
                annotateVariableName(builtin, it.textRange, holder)
            } else if (it is TextVariableArgNamePsiReference) {
                annotateVariableArg(it.variableArg, it.textRange, holder)
            }
        }
    }

}
