package org.javamaster.httpclient.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.reference.support.HttpUrlControllerMethodPsiReference
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpUrlControllerMethodPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val requestTarget = element as HttpRequestTarget

        val originalFile = HttpUtils.getOriginalFile(requestTarget) ?: return PsiReference.EMPTY_ARRAY

        val pathAbsolute = requestTarget.pathAbsolute ?: return PsiReference.EMPTY_ARRAY

        val path = pathAbsolute.text
        val textRange = pathAbsolute.textRangeInParent

        return arrayOf(HttpUrlControllerMethodPsiReference(path, requestTarget, textRange, originalFile))
    }

}