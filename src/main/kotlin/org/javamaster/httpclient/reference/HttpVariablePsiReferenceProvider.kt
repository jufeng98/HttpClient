package org.javamaster.httpclient.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.env.EnvFileService

/**
 * @author yudong
 */
class HttpVariablePsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<PsiReference> {

        val text = element.text
        val start = 2
        val end = text.length - start
        val textRange = TextRange(start, end)
        val name = text.substring(start, end)

        val reference = HttpVariablePsiReference(element, name, textRange)

        return arrayOf(reference)

    }

    class HttpVariablePsiReference(element: PsiElement, private val variableName: String, rangeInElement: TextRange) :
        PsiReferenceBase<PsiElement>(element, rangeInElement) {

        override fun resolve(): PsiElement {
            return HttpVariableFakePsiElement(element, variableName)
        }

        override fun getVariants(): Array<Any> {
            val envVariables = EnvFileService.getEnvVariables(element.project)
            val list = envVariables.entries
                .map {
                    LookupElementBuilder.create(it.key).withTypeText(it.value, true)
                }

            val allList = mutableListOf<Any>()

            allList.addAll(list)

            allList.addAll(builtInFunList)

            return allList.toTypedArray()
        }
    }

    companion object {
        val builtInFunList: Array<Any> by lazy {
            return@lazy InnerVariableEnum.entries
                .map {
                    LookupElementBuilder.create(it.methodName).withTypeText(it.typeText(), true)
                }
                .toList()
                .toTypedArray()
        }
    }
}
