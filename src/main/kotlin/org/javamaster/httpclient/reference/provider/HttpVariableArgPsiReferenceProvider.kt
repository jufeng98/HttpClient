package org.javamaster.httpclient.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpVariableArg
import org.javamaster.httpclient.reference.support.HttpVariableArgPsiReference

/**
 * @author yudong
 */
class HttpVariableArgPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<PsiReference> {
        val variableArg = element as HttpVariableArg

        val textRange = variableArg.textRange
        val range = textRange.shiftLeft(textRange.startOffset)

        return arrayOf(HttpVariableArgPsiReference(variableArg, range))
    }

}
