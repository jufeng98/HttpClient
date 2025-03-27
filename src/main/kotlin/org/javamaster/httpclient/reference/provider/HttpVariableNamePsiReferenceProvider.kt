package org.javamaster.httpclient.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpVariableName
import org.javamaster.httpclient.reference.support.HttpVariableNamePsiReference

/**
 * @author yudong
 */
class HttpVariableNamePsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<PsiReference> {
        val variable = element as HttpVariableName

        val textRange = variable.textRange
        val range = textRange.shiftLeft(textRange.startOffset)

        return arrayOf(HttpVariableNamePsiReference(variable, range))
    }

}
