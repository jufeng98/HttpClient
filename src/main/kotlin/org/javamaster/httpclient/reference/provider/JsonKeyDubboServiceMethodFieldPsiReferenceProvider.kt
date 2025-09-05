package org.javamaster.httpclient.reference.provider

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.reference.support.JsonKeyDubboMethodFieldPsiReference

/**
 * @author yudong
 */
class JsonKeyDubboServiceMethodFieldPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jsonString = element as JsonStringLiteral
        val project = jsonString.project

        if (!jsonString.isPropertyName) {
            return arrayOf()
        }

        val messageBody = InjectedLanguageManager.getInstance(project).getInjectionHost(jsonString)
        if (messageBody !is HttpMessageBody) {
            return arrayOf()
        }

        val httpRequest = PsiTreeUtil.getParentOfType(messageBody, HttpRequest::class.java)!!

        if (httpRequest.method.text != HttpRequestEnum.DUBBO.name) {
            return emptyArray()
        }

        val textRange = jsonString.textRange
        val range = textRange.shiftLeft(textRange.startOffset)

        return arrayOf(JsonKeyDubboMethodFieldPsiReference(jsonString, range))
    }

}