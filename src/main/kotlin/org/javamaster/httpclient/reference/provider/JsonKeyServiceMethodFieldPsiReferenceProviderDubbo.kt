package org.javamaster.httpclient.reference.provider

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.reference.support.JsonKeyPsiReference
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class JsonKeyServiceMethodFieldPsiReferenceProviderDubbo : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jsonString = element as JsonStringLiteral
        val project = jsonString.project

        val messageBody = HttpUtils.getInjectHost(jsonString, project) ?: return arrayOf()

        val httpRequest = PsiTreeUtil.getParentOfType(messageBody, HttpRequest::class.java)!!

        val dubboRequest = HttpUtils.isDubboRequest(httpRequest)
        if (!dubboRequest) {
            return arrayOf()
        }

        val textRange = jsonString.textRange
        val range = textRange.shiftLeft(textRange.startOffset)

        val originalModule = DubboUtils.getOriginalModule(httpRequest) ?: return arrayOf()
        return arrayOf(JsonKeyPsiReference(jsonString, "", originalModule, range))
    }

}