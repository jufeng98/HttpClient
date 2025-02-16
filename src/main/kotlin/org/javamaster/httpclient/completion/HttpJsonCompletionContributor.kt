package org.javamaster.httpclient.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.psi.HttpMessageBody
import org.javamaster.httpclient.reference.HttpVariablePsiReferenceProvider

class HttpJsonCompletionContributor : CompletionContributor() {

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val position = parameters.position
        if (position.parent !is JsonStringLiteral) {
            return
        }

        val project = position.project
        val injectionHost = InjectedLanguageManager.getInstance(project).getInjectionHost(position)
        if (injectionHost !is HttpMessageBody) {
            return
        }

        val prefix = result.prefixMatcher.prefix
        val offset = parameters.offset - position.textRange.startOffset

        val idx = prefix.lastIndexOf("{{", offset)
        if (idx == -1) {
            fillResult(project, result)
            return
        }

        val endIdx = prefix.indexOf("}}", offset)
        val end = if (endIdx == -1 || endIdx <= idx) {
            prefix.length
        } else {
            endIdx
        }

        val str = prefix.substring(idx + 2, end)
        val resultSet = result.withPrefixMatcher(str)
        fillResult(project, resultSet)
    }

    private fun fillResult(project: Project, result: CompletionResultSet) {
        val envVariables = EnvFileService.getEnvVariables(project)
        envVariables.entries
            .forEach {
                val builder = LookupElementBuilder.create(it.key).withTypeText(it.value, true)
                result.addElement(builder)
            }

        HttpVariablePsiReferenceProvider.builtInFunList
            .forEach {
                if (it is LookupElementBuilder) {
                    result.addElement(it)
                }
            }
    }
}
