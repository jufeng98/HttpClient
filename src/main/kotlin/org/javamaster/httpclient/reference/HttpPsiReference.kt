package org.javamaster.httpclient.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.psi.HttpUrl

/**
 * @author yudong
 */
class HttpPsiReference(httpUrl: HttpUrl, private val searchTxt: String, rangeInElement: TextRange) :
    PsiReferenceBase<HttpUrl>(httpUrl, rangeInElement) {

    override fun resolve(): PsiElement {
        return HttpFakePsiElement(myElement, searchTxt)
    }

}
