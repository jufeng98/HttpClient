package org.javamaster.httpclient.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpUrl
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpUrlPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<PsiReference> {
        val httpUrl = element as HttpUrl

        return createReferences(httpUrl)
    }


    private fun createReferences(httpUrl: HttpUrl): Array<PsiReference> {
        val virtualFile = HttpUtils.getOriginalFile(httpUrl) ?: return arrayOf()

        val path = virtualFile.parent?.path ?: return arrayOf()

        val pair = HttpUtils.getSearchTxtInfo(httpUrl, path) ?: return arrayOf()

        val searchTxt = pair.first
        val textRange = pair.second

        val httpPsiReference = HttpPsiReference(httpUrl, searchTxt, textRange)

        return arrayOf(httpPsiReference)
    }
}
