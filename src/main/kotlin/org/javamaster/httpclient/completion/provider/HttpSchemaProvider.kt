package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.psi.HttpRequestTarget

/**
 * @author yudong
 */
class HttpSchemaProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val position = parameters.position
        val method = position.parent?.parent?.prevSibling?.prevSibling?.text ?: return

        val requestEnum = HttpRequestEnum.getInstance(method)

        val variants = requestEnum.getVariants()

        result.addAllElements(variants)

        val requestTargets = PsiTreeUtil.findChildrenOfType(position.containingFile, HttpRequestTarget::class.java)
        requestTargets.forEach {
            val variable = it.variable
            if (variable != null) {
                result.addElement(LookupElementBuilder.create(variable.text))
            }

            val host = it.host
            if (host != null) {
                result.addElement(LookupElementBuilder.create(host.text))
            }
        }
    }

}