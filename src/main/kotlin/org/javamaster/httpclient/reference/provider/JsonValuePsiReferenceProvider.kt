package org.javamaster.httpclient.reference.provider

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.env.EnvFileService.Companion.ENV_FILE_NAMES
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.reference.provider.TextPsiReferenceProvider.Companion.createTextVariableReferences

/**
 * @author yudong
 */
class JsonValuePsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<out PsiReference> {
        val stringLiteral = element as JsonStringLiteral

        if (stringLiteral.isPropertyName) {
            return PsiReference.EMPTY_ARRAY
        }

        val project = stringLiteral.project

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(stringLiteral)
        if (injectionHost is HttpMessageBody) {
            var text = stringLiteral.text
            if (text.length < 4) {
                return PsiReference.EMPTY_ARRAY
            }

            text = text.substring(1, text.length - 1)
            val delta = stringLiteral.textRange.startOffset + 1

            return createTextVariableReferences(stringLiteral, injectionHost, text, delta)
        }

        if (ENV_FILE_NAMES.contains(element.containingFile?.virtualFile?.name)) {
            var text = stringLiteral.text
            text = text.substring(1, text.length - 1)
            val delta = stringLiteral.textRange.startOffset + 1

            return createTextVariableReferences(stringLiteral, null, text, delta)
        }

        return PsiReference.EMPTY_ARRAY
    }

}
