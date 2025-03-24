package org.javamaster.httpclient.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.reference.support.HttpFilePathPsiReference

class HttpFilePathPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val textRange = element.textRange
        val httpFilePath = element as HttpFilePath

        val range = textRange.shiftLeft(textRange.startOffset)

        return arrayOf(HttpFilePathPsiReference(httpFilePath, range))
    }

}
