package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext

/**
 * @author yudong
 */
class HttpSchemaProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters, context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        for (name in list) {
            result.addElement(LookupElementBuilder.create(name))
        }

        for (name in list) {
            result.addElement(LookupElementBuilder.create(name + "localhost:8080"))
        }
    }

    companion object {
        private val list = listOf(
            "http://", "https://", "ws://", "wss://", "dubbo://",
        )
    }

}