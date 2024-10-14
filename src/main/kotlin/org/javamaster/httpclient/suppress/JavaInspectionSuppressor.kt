package org.javamaster.httpclient.suppress

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.psi.HttpGlobalScript
import org.javamaster.httpclient.psi.HttpScript

/**
 * @author yudong
 */
class JavaInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        val injectionHost = InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)
        return injectionHost is HttpScript || injectionHost is HttpGlobalScript
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return arrayOf()
    }
}
