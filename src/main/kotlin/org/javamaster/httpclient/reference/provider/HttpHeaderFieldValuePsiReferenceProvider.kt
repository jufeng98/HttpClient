package org.javamaster.httpclient.reference.provider

import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpHeaderFieldValue
import org.javamaster.httpclient.reference.support.HttpHeaderFieldValuePsiReference

class HttpHeaderFieldValuePsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val textRange = element.textRange
        val fieldValue = element as HttpHeaderFieldValue

        val range = textRange.shiftLeft(textRange.startOffset)

        return arrayOf(HttpHeaderFieldValuePsiReference(fieldValue, range))
    }

}
