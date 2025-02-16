package org.javamaster.httpclient.suppress

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.httpClient.http.request.psi.HttpMessageBody
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement

/**
 * @author yudong
 */
class JsonInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        val injectionHost = InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)
        return injectionHost is HttpMessageBody
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return arrayOf()
    }
}
