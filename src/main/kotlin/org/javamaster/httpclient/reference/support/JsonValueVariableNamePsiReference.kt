package org.javamaster.httpclient.reference.support

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.startOffset
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpVariable

/**
 * @author yudong
 */
class JsonValueVariableNamePsiReference(
    private val stringLiteral: JsonStringLiteral,
    val variable: HttpVariable,
    val textRange: TextRange,
    private val messageBody: HttpMessageBody?,
) : PsiReferenceBase<JsonStringLiteral>(stringLiteral, textRange.shiftLeft(stringLiteral.startOffset), true) {

    override fun resolve(): PsiElement? {
        val psiElement = messageBody ?: stringLiteral

        val variableName = variable.variableName ?: return null
        val name = variableName.name
        val builtin = variableName.isBuiltin

        return HttpVariableNamePsiReference.tryResolveVariable(name, builtin, psiElement, false)
    }

    override fun getVariants(): Array<Any> {
        return HttpVariableNamePsiReference.getVariableVariants(element)
    }
}