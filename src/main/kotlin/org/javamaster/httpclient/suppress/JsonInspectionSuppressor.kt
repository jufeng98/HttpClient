package org.javamaster.httpclient.suppress

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import org.javamaster.httpclient.inspection.MyJsonInspection
import org.javamaster.httpclient.psi.HttpMessageBody

/**
 * @author yudong
 */
class JsonInspectionSuppressor : InspectionSuppressor {
    private val notSuppressSet = setOf(
        SpellCheckingInspection::class.java.simpleName,
        MyJsonInspection::class.java.simpleName,
    )

    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (notSuppressSet.contains(toolId)) {
            return false
        }

        val injectionHost = InjectedLanguageManager.getInstance(element.project).getInjectionHost(element)
        return injectionHost is HttpMessageBody
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return arrayOf()
    }
}
