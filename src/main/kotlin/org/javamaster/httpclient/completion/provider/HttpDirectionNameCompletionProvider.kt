package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.AddSpaceInsertHandler
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpDirectionNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        var builder = LookupElementBuilder.create(HttpUtils.CONNECT_TIMEOUT_NAME)
            .withTypeText("Http and websocket connect timeout(positive number, unit: seconds)", true)
            .withInsertHandler(AddSpaceInsertHandler.INSTANCE)
        result.addElement(builder)

        builder = LookupElementBuilder.create(HttpUtils.READ_TIMEOUT_NAME)
            .withTypeText("Http and websocket read timeout(positive number, unit: seconds)", true)
            .withInsertHandler(AddSpaceInsertHandler.INSTANCE)
        result.addElement(builder)

        builder = LookupElementBuilder.create(HttpUtils.TIMEOUT_NAME)
            .withTypeText("Dubbo connect timeout(positive number, unit: milliseconds)", true)
            .withInsertHandler(AddSpaceInsertHandler.INSTANCE)
        result.addElement(builder)
    }

}
