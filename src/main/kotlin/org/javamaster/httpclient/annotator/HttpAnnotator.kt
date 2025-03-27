package org.javamaster.httpclient.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateRequestName
import org.javamaster.httpclient.annotator.VariableAnnotator.annotateVariableName
import org.javamaster.httpclient.psi.HttpComment
import org.javamaster.httpclient.psi.HttpVariableName

/**
 * @author yudong
 */
class HttpAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is HttpVariableName) {
            annotateVariableName(element.isBuiltin, element.textRange, holder)
        } else if (element is HttpComment) {
            annotateRequestName(element.textRange, holder)
        }
    }

}
