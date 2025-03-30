package org.javamaster.httpclient.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateQueryName
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateQueryValue
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariableArg
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariableName
import org.javamaster.httpclient.reference.support.QueryNamePsiReference
import org.javamaster.httpclient.reference.support.QueryValuePsiReference
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
            when (it) {
                is TextVariableNamePsiReference -> {
                    val builtin = it.variable.variableName?.isBuiltin ?: false
                    annotateVariableName(builtin, it.textRange, holder)
                }

                is TextVariableArgNamePsiReference -> {
                    annotateVariableArg(it.variableArg, it.textRange, holder)
                }

                is QueryNamePsiReference -> {
                    annotateQueryName(it.textRange, holder)
                }

                is QueryValuePsiReference -> {
                    annotateQueryValue(it.textRange, holder)
                }
            }
        }
    }

}
