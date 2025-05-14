package org.javamaster.httpclient.reference.provider

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.psi.impl.TextVariableLazyFileElement
import org.javamaster.httpclient.psi.impl.UrlEncodedLazyFileElement
import org.javamaster.httpclient.reference.support.QueryNamePsiReference
import org.javamaster.httpclient.reference.support.QueryValuePsiReference
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

        val parent = (injectionHost as HttpMessageBody).parent.parent

        val request = PsiTreeUtil.getParentOfType(injectionHost, HttpRequest::class.java)!!

        if (request.contentType == ContentType.APPLICATION_FORM_URLENCODED
            || (parent is HttpMultipartField && parent.contentType == ContentType.APPLICATION_FORM_URLENCODED)
        ) {
            val query = UrlEncodedLazyFileElement.parse(text) ?: return emptyArray()

            val references = request.requestTarget!!.references

            val controllerMethod = if (references.isNotEmpty()) {
                references[0].resolve() as PsiMethod?
            } else {
                null
            }

            return createUrlEncodedReferences(plainTextFile, injectionHost, query, delta, controllerMethod)
        }

        return createTextVariableReferences(plainTextFile, injectionHost, text, delta)
    }

    private fun createUrlEncodedReferences(
        psiElement: PsiElement,
        messageBody: HttpMessageBody?,
        query: HttpQuery,
        delta: Int,
        controllerMethod: PsiMethod?,
    ): Array<PsiReference> {
        val references = mutableListOf<PsiReference>()

        query.queryParameterList
            .forEach {
                val queryParameterName = it.queryParameterKey
                val queryName = queryParameterName.text

                val nameRange = queryParameterName.textRange.shiftRight(delta)

                val nameReference = QueryNamePsiReference(psiElement, nameRange, controllerMethod, queryName)
                references.add(nameReference)

                val queryParameterValue = it.queryParameterValue ?: return@forEach
                val variable = queryParameterValue.variable
                if (variable != null) {
                    val list = createVariableReferences(variable, psiElement, messageBody, delta)
                    references.addAll(list)
                } else {
                    val valueRange = queryParameterValue.textRange.shiftRight(delta)

                    val valueReference = QueryValuePsiReference(psiElement, valueRange)
                    references.add(valueReference)
                }
            }

        return references.toTypedArray()
    }

    companion object {

        fun createTextVariableReferences(
            psiElement: PsiElement,
            messageBody: HttpMessageBody?,
            text: String,
            delta: Int,
        ): Array<out PsiReference> {
            val textVariable = TextVariableLazyFileElement.parse(text)

            return textVariable.variableList
                .map {
                    createVariableReferences(it, psiElement, messageBody, delta)
                }
                .flatten()
                .toTypedArray()
        }

        private fun createVariableReferences(
            variable: HttpVariable,
            psiElement: PsiElement,
            messageBody: HttpMessageBody?,
            delta: Int,
        ): MutableList<PsiReference> {
            val variableName = variable.variableName ?: return mutableListOf()

            val nameRange = variableName.textRange
            if (nameRange.startOffset == nameRange.endOffset) {
                return mutableListOf()
            }

            val range = nameRange.shiftRight(delta)
            val reference = TextVariableNamePsiReference(psiElement, variable, range, messageBody)

            val references = mutableListOf<PsiReference>(reference)

            val argReferences = variable.variableArgs?.variableArgList
                ?.map { arg ->
                    val argRange = arg.textRange.shiftRight(delta)
                    TextVariableArgNamePsiReference(psiElement, arg, argRange, messageBody)
                }
                ?: emptyList()

            references.addAll(argReferences)

            return references
        }

    }

}
