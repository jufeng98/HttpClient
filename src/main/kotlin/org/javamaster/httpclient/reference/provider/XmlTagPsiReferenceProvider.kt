package org.javamaster.httpclient.reference.provider

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.reference.provider.TextPsiReferenceProvider.Companion.createTextVariableReferences

/**
 * @author yudong
 */
class XmlTagPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<out PsiReference> {
        val injectionHost = InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)
        if (injectionHost !is HttpMessageBody) {
            return PsiReference.EMPTY_ARRAY
        }

        val text = element.text
        val delta = element.textRange.startOffset

        return createTextVariableReferences(element, injectionHost, text, delta)
    }

}
