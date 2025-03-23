package org.javamaster.httpclient.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.reference.support.HttpVariablePsiReferenceProvider
import org.javamaster.httpclient.utils.HttpUtils

class JsonValueReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext,
                ): Array<PsiReference> {
                    val literal = element as JsonStringLiteral

                    if (isRefValue(literal)) {
                        return arrayOf(JsonRefReference(literal))
                    }

                    return PsiReference.EMPTY_ARRAY
                }
            })
    }

    private fun isRefValue(literal: JsonStringLiteral): Boolean {
        if (literal.isPropertyName) {
            return false
        }
        val value = literal.value
        return value.startsWith(HttpUtils.VARIABLE_SIGN_START)
    }


    class JsonRefReference(private val jsonString: JsonStringLiteral) :
        PsiReferenceBase<JsonStringLiteral?>(jsonString) {
        override fun resolve(): PsiElement? {
            val injectionHost = InjectedLanguageManager.getInstance(jsonString.project).getInjectionHost(jsonString)
            if (injectionHost !is HttpMessageBody) {
                return null
            }

            val value = jsonString.value
            if (!value.startsWith(HttpUtils.VARIABLE_SIGN_START)) {
                return null
            }

            val variableName = value.substring(2, value.length - 2)

            return HttpVariablePsiReferenceProvider.tryResolveVariable(variableName, injectionHost, false)
        }

        override fun getVariants(): Array<Any> {
            return arrayOf()
        }
    }
}

