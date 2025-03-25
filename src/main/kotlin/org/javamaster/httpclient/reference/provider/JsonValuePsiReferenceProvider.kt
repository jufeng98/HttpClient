package org.javamaster.httpclient.reference.provider

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.env.EnvFileService.Companion.ENV_FILE_NAMES
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.reference.support.JsonValueVariablePsiReference
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class JsonValuePsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext,
    ): Array<out PsiReference> {
        val stringLiteral = element as JsonStringLiteral

        if (stringLiteral.isPropertyName) {
            return PsiReference.EMPTY_ARRAY
        }

        val project = stringLiteral.project

        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(stringLiteral)
        if (injectionHost is HttpMessageBody) {
            return createReferences(stringLiteral, injectionHost)
        }

        if (ENV_FILE_NAMES.contains(element.containingFile?.virtualFile?.name)) {
            return createReferences(stringLiteral, element)
        }

        return PsiReference.EMPTY_ARRAY
    }

    private fun createReferences(
        literal: JsonStringLiteral,
        element: PsiElement,
    ): Array<JsonValueVariablePsiReference> {
        if (literal.isPropertyName) {
            return emptyArray()
        }

        val value = literal.text
        val list = mutableListOf<JsonValueVariablePsiReference>()

        var pair = findVariablePair(value, 0)
        while (pair != null) {
            val textRange = TextRange(pair.first, pair.second)
            val variableName = value.substring(pair.first, pair.second)
            val builtin = variableName.startsWith("$")
            val reference = JsonValueVariablePsiReference(literal, builtin, variableName, textRange, element)

            list.add(reference)

            pair = findVariablePair(value, pair.second + 2)
        }

        return list.toTypedArray()
    }

    private fun findVariablePair(value: String, start: Int): Pair<Int, Int>? {
        var startIdx = value.indexOf(HttpUtils.VARIABLE_SIGN_START, start)
        if (startIdx == -1) {
            return null
        }

        startIdx += 2

        val endIdx = value.indexOf(HttpUtils.VARIABLE_SIGN_END, startIdx)
        if (endIdx == -1) {
            return null
        }

        if (startIdx == endIdx) {
            return null
        }

        return Pair(startIdx, endIdx)
    }

}
