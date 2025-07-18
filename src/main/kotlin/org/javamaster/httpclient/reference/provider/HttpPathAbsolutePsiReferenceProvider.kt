package org.javamaster.httpclient.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpPathAbsolute
import org.javamaster.httpclient.reference.support.HttpPathAbsolutePsiReference

/**
 * @author yudong
 */
class HttpPathAbsolutePsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val httpPathAbsolute = element as HttpPathAbsolute

        val textRange = httpPathAbsolute.textRange
        val range = textRange.shiftLeft(textRange.startOffset)

        return arrayOf(HttpPathAbsolutePsiReference(httpPathAbsolute, range))
    }

}