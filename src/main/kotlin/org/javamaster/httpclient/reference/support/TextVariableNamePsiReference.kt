package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpVariable

/**
 * @author yudong
 */
class TextVariableNamePsiReference(
    private val psiElement: PsiElement,
    val variable: HttpVariable,
    val textRange: TextRange,
    private val messageBody: HttpMessageBody?,
) : PsiReferenceBase<PsiElement>(psiElement, textRange.shiftLeft(psiElement.textRange.startOffset), true) {

    override fun resolve(): PsiElement? {
        val psiElement = messageBody ?: psiElement

        val variableName = variable.variableName ?: return null
        val name = variableName.name
        val builtin = variableName.isBuiltin

        return HttpVariableNamePsiReference.tryResolveVariable(name, builtin, psiElement, false)
    }

    override fun getVariants(): Array<Any> {
        return HttpVariableNamePsiReference.getVariableVariants(element)
    }
}