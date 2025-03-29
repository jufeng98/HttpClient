package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.doc.support.CoolRequestHelper
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpRequestTarget

/**
 * @author yudong
 */
class HttpUrlControllerMethodPsiReference(
    private val searchTxt: String,
    private val requestTarget: HttpRequestTarget,
    textRange: TextRange,
) :
    PsiReferenceBase<HttpRequestTarget>(requestTarget, textRange) {

    override fun resolve(): PsiElement? {
        val virtualFile = element.containingFile.virtualFile

        val module = CoolRequestHelper.findModule(requestTarget, virtualFile) ?: return null

        val httpMethod = PsiTreeUtil.getPrevSiblingOfType(requestTarget, HttpMethod::class.java)!!

        return CoolRequestHelper.findMethod(module, searchTxt, httpMethod.text)
    }

}
