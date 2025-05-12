package org.javamaster.httpclient.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpQueryParameterKey
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.reference.support.QueryNamePsiReference

class HttpQueryParameterKeyPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val textRange = element.textRange
        val queryParameterKey = element as HttpQueryParameterKey
        val queryName = queryParameterKey.text

        val request = PsiTreeUtil.getParentOfType(queryParameterKey, HttpRequest::class.java)!!

        val references = request.requestTarget!!.references
        if (references.isEmpty()) {
            return emptyArray()
        }

        val controllerMethod = references[0].resolve() as PsiMethod? ?: return emptyArray()

        val range = textRange.shiftLeft(textRange.startOffset)

        return arrayOf(QueryNamePsiReference(queryParameterKey, range, controllerMethod, queryName))
    }

}
