package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiErrorElement
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.psi.HttpMethod

/**
 * @author yudong
 */
class HttpMethodsProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        if (!isRequestStart(parameters)) {
            return
        }

        HttpRequestEnum.entries.forEach {
            result.addElement(
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create(it.name)
                        .withBoldness(true)
                        .withInsertHandler(AddSpaceInsertHandler.INSTANCE), 300.0
                )
            )
        }
    }

    private fun isRequestStart(parameters: CompletionParameters): Boolean {
        val parent = parameters.position.parent
        return parent is PsiErrorElement || parent is HttpMethod
    }
}