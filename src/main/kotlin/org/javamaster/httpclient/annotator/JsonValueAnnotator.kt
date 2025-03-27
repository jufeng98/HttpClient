package org.javamaster.httpclient.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariableArg
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariableName
import org.javamaster.httpclient.reference.support.JsonValueArgNamePsiReference
import org.javamaster.httpclient.reference.support.JsonValueVariableNamePsiReference

/**
 * @author yudong
 */
class JsonValueAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val references = element.references
        if (references.isEmpty()) {
            return
        }

        references.forEach {
            if (it is JsonValueVariableNamePsiReference) {
                val builtin = it.variable.variableName?.isBuiltin ?: false
                annotateVariableName(builtin, it.textRange, holder)
            } else if (it is JsonValueArgNamePsiReference) {
                annotateVariableArg(it.variableArg, it.textRange, holder)
            }
        }
    }

}
