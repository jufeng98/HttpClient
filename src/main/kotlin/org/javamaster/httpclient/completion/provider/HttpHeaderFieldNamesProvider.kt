package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.completion.support.HttpHeadersDictionary
import org.javamaster.httpclient.completion.support.HttpSuffixInsertHandler
import org.javamaster.httpclient.psi.HttpRequest

/**
 * @author yudong
 */
class HttpHeaderFieldNamesProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters, context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val request = PsiTreeUtil.getParentOfType(parameters.position, HttpRequest::class.java)
        val method = request?.method?.text

        if (method == HttpRequestEnum.DUBBO.name) {
            HttpHeadersDictionary.dubboHeaderNames.forEach {
                val builder = LookupElementBuilder.create(it)
                    .withCaseSensitivity(false)
                    .withInsertHandler(HttpSuffixInsertHandler.FIELD_SEPARATOR)
                result.addElement(builder)
            }
            return
        }

        for (header in HttpHeadersDictionary.headerMap.values) {
            val priority = PrioritizedLookupElement.withPriority(
                LookupElementBuilder.create(header, header.name)
                    .withCaseSensitivity(false)
                    .withStrikeoutness(header.isDeprecated)
                    .withInsertHandler(HttpSuffixInsertHandler.FIELD_SEPARATOR),
                if (header.isDeprecated) 100.0 else 200.0
            )
            result.addElement(priority)
        }
    }
}