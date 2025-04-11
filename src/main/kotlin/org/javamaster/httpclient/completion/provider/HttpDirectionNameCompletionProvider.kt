package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.psi.HttpRequestBlock

/**
 * @author yudong
 */
class HttpDirectionNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val requestBlock = PsiTreeUtil.getParentOfType(parameters.position, HttpRequestBlock::class.java)

        val params = if (requestBlock != null) {
            ParamEnum.getRequestParams()
        } else {
            ParamEnum.getGlobalParams()
        }

        params.forEach {
            val builder = LookupElementBuilder.create(it.param)
                .withTypeText(it.desc, true)
                .withInsertHandler(it.insertHandler())
            result.addElement(builder)
        }
    }

}
