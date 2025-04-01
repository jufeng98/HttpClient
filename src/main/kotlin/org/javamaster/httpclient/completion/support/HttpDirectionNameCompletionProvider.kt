package org.javamaster.httpclient.completion.support

import com.intellij.codeInsight.completion.AddSpaceInsertHandler
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.utils.HttpUtils

class HttpDirectionNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val builder1 = LookupElementBuilder.create(HttpUtils.CONNECT_TIMEOUT_NAME)
            .withTypeText("http and websocket connect timeout(positive number,unit: seconds)", true)
            .withInsertHandler(AddSpaceInsertHandler.INSTANCE)
        val builder2 = LookupElementBuilder.create(HttpUtils.READ_TIMEOUT_NAME)
            .withTypeText("http and websocket read timeout(positive number,unit: seconds)", true)
            .withInsertHandler(AddSpaceInsertHandler.INSTANCE)
        val builder3 = LookupElementBuilder.create(HttpUtils.TIMEOUT_NAME)
            .withTypeText("dubbo connect timeout(positive number,unit: millisecond)", true)
            .withInsertHandler(AddSpaceInsertHandler.INSTANCE)

        result.addElement(builder1)
        result.addElement(builder2)
        result.addElement(builder3)
    }

}
