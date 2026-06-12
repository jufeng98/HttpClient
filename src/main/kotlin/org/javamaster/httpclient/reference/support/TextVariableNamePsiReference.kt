package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
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
) : PsiPolyVariantReferenceBase<PsiElement>(psiElement, textRange.shiftLeft(psiElement.textRange.startOffset), true) {

    override fun resolve(): PsiElement? {
        val results = multiResolve(false)

        return if (results.isEmpty()) null else results[0].element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val psiElement = messageBody ?: psiElement

        val variableName = variable.variableName ?: return emptyArray()
        val name = variableName.name
        val builtin = variableName.isBuiltin

        val elements = HttpVariableNamePsiReference.tryResolveVariable(name, builtin, psiElement, true)

        return PsiElementResolveResult.createResults(elements)
    }

    override fun getVariants(): Array<Any> {
        return HttpVariableNamePsiReference.getVariableVariants(element)
    }

}