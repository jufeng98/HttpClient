package org.javamaster.httpclient.reference.support

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.psi.HttpOutputFilePath
import org.javamaster.httpclient.resolve.VariableResolver.Companion.ENV_PREFIX
import org.javamaster.httpclient.resolve.VariableResolver.Companion.PROPERTY_PREFIX
import org.javamaster.httpclient.utils.HttpUtils

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
        if (end <= start) {
            return arrayOf()
        }

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
            val allList = mutableListOf<Any>()

            if (element.parent is HttpOutputFilePath) {
                allList.add(HttpUtils.PROJECT_ROOT)
                allList.add(HttpUtils.HISTORY_FOLDER)
                allList.add(HttpUtils.MVN_TARGET)
                return allList.toTypedArray()
            }

            val envVariables = EnvFileService.getEnvVariables(element.project)
            val list = envVariables.entries
                .map {
                    LookupElementBuilder.create(it.key).withTypeText(it.value, true)
                }

            allList.addAll(list)

            allList.addAll(builtInFunList)

            val propertyList = System.getProperties().entries
                .map {
                    LookupElementBuilder.create(PROPERTY_PREFIX + "." + it.key)
                }
                .toList()
            allList.addAll(propertyList)

            val envList = System.getenv().entries
                .map {
                    LookupElementBuilder.create(ENV_PREFIX + "." + it.key)
                }
                .toList()
            allList.addAll(envList)

            return allList.toTypedArray()
        }
    }

    companion object {
        val builtInFunList: Array<Any> by lazy {
            return@lazy InnerVariableEnum.entries
                .map {
                    LookupElementBuilder.create(it.methodName)
                        .withInsertHandler(it.insertHandler())
                        .withTypeText(it.typeText(), true)
                }
                .toList()
                .toTypedArray()
        }
    }
}
