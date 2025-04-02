package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.psi.HttpVariableArg

/**
 * @author yudong
 */
class TextVariableArgNamePsiReference(
    private val psiElement: PsiElement,
    val variableArg: HttpVariableArg,
    val textRange: TextRange,
    private val messageBody: HttpMessageBody?,
) : PsiReferenceBase<PsiElement>(psiElement, textRange.shiftLeft(psiElement.textRange.startOffset), true) {

    override fun resolve(): PsiElement? {
        val psiElement = messageBody ?: psiElement
        val httpFileParentPath = psiElement.containingFile.virtualFile?.parent?.path ?: return null

        val guessPath = variableArg.value.toString()

        return HttpVariableArgPsiReference.tryResolvePath(guessPath, httpFileParentPath, element.project)
    }

    override fun getVariants(): Array<Any> {
        return emptyArray()
    }

}