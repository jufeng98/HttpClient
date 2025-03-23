package org.javamaster.httpclient.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariable
import org.javamaster.httpclient.psi.HttpPsiUtils
import org.javamaster.httpclient.psi.HttpTypes
import org.javamaster.httpclient.psi.HttpVariable

/**
 * @author yudong
 */
class HttpVariableAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is HttpVariable) return

        val dollar = HttpPsiUtils.getNextSiblingByType(element.firstChild, HttpTypes.DOLLAR, false)
        annotateVariable(dollar != null, element.textRange, holder)
    }

}
