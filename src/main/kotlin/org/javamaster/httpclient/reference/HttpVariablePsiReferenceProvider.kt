package org.javamaster.httpclient.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.resolve.VariableResolver.Companion.PATTERN
import kotlin.streams.toList

/**
 * @author yudong
 */
class HttpVariablePsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<PsiReference> {
        return createReferences(element)
    }

    private fun createReferences(element: PsiElement): Array<PsiReference> {
        val matcher = PATTERN.matcher(element.text)

        return matcher.results()
            .map {
                val matchStr = it.group()
                val variableName = matchStr.substring(2, matchStr.length - 2)
                val textRange = TextRange(it.start(), it.end())
                HttpVariablePsiReference(element, variableName, textRange)
            }
            .toList()
            .toTypedArray()
    }
}
