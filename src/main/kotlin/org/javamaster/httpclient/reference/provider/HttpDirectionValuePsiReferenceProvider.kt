package org.javamaster.httpclient.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpDirectionValue
import org.javamaster.httpclient.reference.support.HttpDirectionValuePsiReference

class HttpDirectionValuePsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val textRange = element.textRange
        val directionValue = element as HttpDirectionValue

        val range = textRange.shiftLeft(textRange.startOffset)

        return arrayOf(HttpDirectionValuePsiReference(directionValue, range))
    }

}
