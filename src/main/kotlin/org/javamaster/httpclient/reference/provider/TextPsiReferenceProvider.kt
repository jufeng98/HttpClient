package org.javamaster.httpclient.reference.provider

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainTextFile
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.impl.TextVariableLazyFileElement
import org.javamaster.httpclient.reference.support.TextVariableArgNamePsiReference
import org.javamaster.httpclient.reference.support.TextVariableNamePsiReference

/**
 * @author yudong
 */
class TextPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<out PsiReference> {
        val plainTextFile = element as PsiPlainTextFile
        val project = plainTextFile.project

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(plainTextFile)
        if (injectionHost !is HttpMessageBody) {
            return PsiReference.EMPTY_ARRAY
        }

        val text = plainTextFile.text
        val delta = plainTextFile.textRange.startOffset

        return createTextVariableReferences(plainTextFile, injectionHost, text, delta)
    }

    companion object {

        fun createTextVariableReferences(
            psiElement: PsiElement,
            messageBody: HttpMessageBody?,
            text: String,
            delta: Int,
        ): Array<out PsiReference> {
            val myJsonValue = TextVariableLazyFileElement.parse(text)

            return myJsonValue.variableList
                .mapNotNull {
                    val variableName = it.variableName ?: return@mapNotNull null

                    val nameRange = variableName.textRange
                    if (nameRange.startOffset == nameRange.endOffset) {
                        return@mapNotNull null
                    }

                    val range = nameRange.shiftRight(delta)
                    val reference = TextVariableNamePsiReference(psiElement, it, range, messageBody)

                    val references = mutableListOf<PsiReference>(reference)

                    val argReferences = it.variableArgs?.variableArgList
                        ?.map { arg ->
                            val argRange = arg.textRange.shiftRight(delta)
                            TextVariableArgNamePsiReference(psiElement, arg, argRange, messageBody)
                        }
                        ?: emptyList()

                    references.addAll(argReferences)

                    references
                }
                .flatten()
                .toTypedArray()
        }

    }

}
