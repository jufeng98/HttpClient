package org.javamaster.httpclient.reference.support

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * @author yudong
 */
class JsonValueVariablePsiReference(
    jsonString: JsonStringLiteral,
    val variableName: String,
    val textRange: TextRange,
    private val psiElement: PsiElement,
) : PsiReferenceBase<JsonStringLiteral>(jsonString, textRange, true) {

    override fun resolve(): PsiElement? {
        return HttpVariablePsiReference.tryResolveVariable(variableName, psiElement, false)
    }

    override fun getVariants(): Array<Any> {
        return HttpVariablePsiReference.getVariableVariants(element)
    }
}