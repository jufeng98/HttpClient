package org.javamaster.httpclient.reference.support

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.startOffset
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpVariableArg

/**
 * @author yudong
 */
class JsonValueArgNamePsiReference(
    private val stringLiteral: JsonStringLiteral,
    val variableArg: HttpVariableArg,
    val textRange: TextRange,
    private val messageBody: HttpMessageBody?,
) : PsiReferenceBase<JsonStringLiteral>(stringLiteral, textRange.shiftLeft(stringLiteral.startOffset), true) {

    override fun resolve(): PsiElement? {
        val psiElement = messageBody ?: stringLiteral
        val httpFileParentPath = psiElement.containingFile.virtualFile?.parent?.path ?: return null

        val guessPath = variableArg.value.toString()

        return HttpVariableArgPsiReference.tryResolvePath(guessPath, httpFileParentPath, element.project)
    }

    override fun getVariants(): Array<Any> {
        return emptyArray()
    }

}