package org.javamaster.httpclient.reference.provider

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.env.EnvFileService.Companion.ENV_FILE_NAMES
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.impl.MyJsonLazyFileElement
import org.javamaster.httpclient.reference.support.JsonValueArgNamePsiReference
import org.javamaster.httpclient.reference.support.JsonValueVariableNamePsiReference

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
            return createReferences(stringLiteral, injectionHost)
        }

        if (ENV_FILE_NAMES.contains(element.containingFile?.virtualFile?.name)) {
            return createReferences(stringLiteral, null)
        }

        return PsiReference.EMPTY_ARRAY
    }

    private fun createReferences(
        stringLiteral: JsonStringLiteral,
        messageBody: HttpMessageBody?,
    ): Array<out PsiReference> {
        val jsonValueText = stringLiteral.text
        val value = jsonValueText.substring(1, jsonValueText.length - 1)

        val literalRange = stringLiteral.textRange

        val myJsonValue = MyJsonLazyFileElement.parse(value)

        return myJsonValue.variableList
            .mapNotNull {
                val variableName = it.variableName ?: return@mapNotNull null

                val nameRange = variableName.textRange
                if (nameRange.startOffset == nameRange.endOffset) {
                    return@mapNotNull null
                }

                val delta = literalRange.startOffset + 1

                val range = nameRange.shiftRight(delta)
                val reference = JsonValueVariableNamePsiReference(stringLiteral, it, range, messageBody)

                val references = mutableListOf<PsiReference>(reference)

                val argReferences = it.variableArgs?.variableArgList
                    ?.map { arg ->
                        val argRange = arg.textRange.shiftRight(delta)
                        JsonValueArgNamePsiReference(stringLiteral, arg, argRange, messageBody)
                    }
                    ?: emptyList()

                references.addAll(argReferences)

                references
            }
            .flatten()
            .toTypedArray()

    }

}
