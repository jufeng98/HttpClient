package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.utils.DubboUtils.fillTargetDubboMethodParams
import org.javamaster.httpclient.utils.DubboUtils.getTargetPsiFieldClass
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.resolveUrlControllerTargetPsiClass

/**
 * @author yudong
 */
class JsonKeyCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val psiElement = parameters.position
        val currentJsonString = psiElement.parent as JsonStringLiteral

        var targetPsiClass = resolveUrlControllerTargetPsiClass(currentJsonString)
        if (targetPsiClass == null) {
            val filled = fillTargetDubboMethodParams(currentJsonString, result, "")
            if (filled) {
                return
            }

            targetPsiClass = getTargetPsiFieldClass(currentJsonString, false)
        }

        targetPsiClass ?: return

        val prefixMatcher = result.prefixMatcher
        val prefix = prefixMatcher.prefix
        if (prefix.length < 2) {
            return
        }

        val newPrefix = prefix.substring(1)
        val completionResultSet = result.withPrefixMatcher(newPrefix)

        targetPsiClass.fields
            .forEach {
                if (it.modifierList?.hasModifierProperty("static") == true) {
                    return@forEach
                }

                val typeText = it.type.presentableText + " " + HttpUtils.getPsiFieldDesc(it)

                val builder = LookupElementBuilder
                    .create(it)
                    .withTypeText(typeText, true)

                completionResultSet.addElement(builder)
            }
    }

}
