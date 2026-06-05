package org.javamaster.httpclient.reference.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.reference.support.HttpUrlControllerMethodPsiReference
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.MyPsiUtils

/**
 * @author yudong
 */
class HttpUrlControllerMethodPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val requestTarget = element as HttpRequestTarget

        val originalFile = HttpUtils.getOriginalFile(requestTarget) ?: return PsiReference.EMPTY_ARRAY

        val path = originalFile.parent?.path ?: return PsiReference.EMPTY_ARRAY

        val pair = MyPsiUtils.getSearchTxtInfo(requestTarget, path) ?: return PsiReference.EMPTY_ARRAY

        val searchTxt = pair.first
        val textRange = pair.second

        return arrayOf(HttpUrlControllerMethodPsiReference(searchTxt, requestTarget, textRange, originalFile))
    }

}