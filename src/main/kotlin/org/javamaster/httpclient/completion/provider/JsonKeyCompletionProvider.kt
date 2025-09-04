package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpRequest
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.PsiUtils

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
        val jsonStringLiteral = psiElement.parent as JsonStringLiteral

        val virtualFile = PsiUtil.getVirtualFile(jsonStringLiteral) ?: return

        val project = jsonStringLiteral.project

        val messageBody = HttpUtils.getInjectHost(jsonStringLiteral, project) ?: return

        val httpRequest = PsiTreeUtil.getParentOfType(messageBody, HttpRequest::class.java)!!

        val references = httpRequest.requestTarget!!.references
        if (references.isEmpty()) {
            return
        }

        val controllerMethod = references[0].resolve() as PsiMethod? ?: return

        val paramPsiType = if (virtualFile.name.endsWith("res.http") == true) {
            controllerMethod.returnType
        } else {
            HttpUtils.resolveTargetParam(controllerMethod)?.type
        }

        val paramPsiCls = PsiUtils.resolvePsiType(paramPsiType) ?: return

        val prefixMatcher = result.prefixMatcher
        val newPrefix = prefixMatcher.prefix.substring(1)
        val completionResultSet = result.withPrefixMatcher(newPrefix)

        paramPsiCls.fields
            .forEach {
                val psiClass = PsiUtils.resolvePsiType(it.type)

                val typeText = if (psiClass != null) {
                    psiClass.name + " " + HttpUtils.getPsiElementDesc(it)
                } else {
                    it.name + " " + HttpUtils.getPsiElementDesc(it)
                }

                val builder = LookupElementBuilder
                    .create(it.name)
                    .withTypeText(typeText, true)

                completionResultSet.addElement(builder)
            }
    }

}
