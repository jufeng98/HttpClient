package org.javamaster.httpclient.reference.provider

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.reference.support.JsonKeyControllerMethodFieldPsiReference
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class JsonKeyControllerMethodFieldPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jsonString = element as JsonStringLiteral
        val project = jsonString.project

        val messageBody = HttpUtils.getInjectHost(jsonString, project) ?: return emptyArray()

        val httpRequest = PsiTreeUtil.getParentOfType(messageBody, HttpRequest::class.java)!!

        val references = httpRequest.requestTarget!!.references
        if (references.isEmpty()) {
            return emptyArray()
        }

        val controllerMethod = references[0].resolve() as PsiMethod? ?: return emptyArray()

        val textRange = jsonString.textRange
        val range = textRange.shiftLeft(textRange.startOffset)

        return arrayOf(JsonKeyControllerMethodFieldPsiReference(jsonString, controllerMethod, range))
    }

}