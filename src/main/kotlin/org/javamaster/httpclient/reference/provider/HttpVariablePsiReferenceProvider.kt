package org.javamaster.httpclient.reference.provider

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpVariable
import org.javamaster.httpclient.reference.support.HttpVariablePsiReference

/**
 * @author yudong
 */
class HttpVariablePsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<PsiReference> {
        val variable = element as HttpVariable

        val rangeInParent = element.textRangeInParent
        val start = rangeInParent.startOffset + 2
        val end = rangeInParent.endOffset - 2

        if (end <= start) {
            return PsiReference.EMPTY_ARRAY
        }

        val range = TextRange(start, end)

        return arrayOf(HttpVariablePsiReference(element, variable.isBuiltin, variable.name, range))
    }

}
