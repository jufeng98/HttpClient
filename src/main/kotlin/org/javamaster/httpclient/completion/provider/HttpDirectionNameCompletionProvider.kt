package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.AddSpaceInsertHandler
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.enums.ParamEnum

/**
 * @author yudong
 */
class HttpDirectionNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        ParamEnum.entries
            .forEach {
                val builder = LookupElementBuilder.create(it.param)
                    .withTypeText(it.desc, true)
                    .withInsertHandler(AddSpaceInsertHandler.INSTANCE)
                result.addElement(builder)
            }
    }

}
