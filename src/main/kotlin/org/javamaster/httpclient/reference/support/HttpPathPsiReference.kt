package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.psi.HttpRequestTarget

/**
 * @author yudong
 */
class HttpPathPsiReference(private val searchTxt: String, requestTarget: HttpRequestTarget, textRange: TextRange) :
    PsiReferenceBase<HttpRequestTarget>(requestTarget, textRange) {

    override fun resolve(): PsiElement {
        return HttpControllerMethodPsiElement(element, searchTxt)
    }

}
