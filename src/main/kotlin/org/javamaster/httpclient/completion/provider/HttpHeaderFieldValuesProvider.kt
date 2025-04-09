package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.completion.support.HttpHeadersDictionary.headerValuesMap
import org.javamaster.httpclient.psi.HttpHeader
import org.javamaster.httpclient.psi.HttpHeaderField
import org.javamaster.httpclient.utils.DubboUtils
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpHeaderFieldValuesProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val headerField = PsiTreeUtil.getParentOfType(
            CompletionUtil.getOriginalOrSelf(parameters.position),
            HttpHeaderField::class.java
        )
        val headerName = headerField?.headerFieldName?.text
        if (StringUtil.isEmpty(headerName)) {
            return
        }

        val headerValues = headerValuesMap[headerName]
        if (headerValues != null) {
            headerValues.forEach {
                result.addElement(PrioritizedLookupElement.withPriority(LookupElementBuilder.create(it), 200.0))
            }
            return
        }

        if (headerName.equals(DubboUtils.INTERFACE_KEY, ignoreCase = true)) {
            val newResult = result.withPrefixMatcher(CompletionUtil.findReferenceOrAlphanumericPrefix(parameters))
            JavaClassNameCompletionContributor.addAllClasses(
                parameters,
                parameters.invocationCount <= 1,
                newResult.prefixMatcher,
                newResult
            )
            return
        }

        if (headerName.equals(DubboUtils.METHOD_KEY, ignoreCase = true)) {
            val header = headerField!!.parent as HttpHeader
            val interfaceField = header.interfaceField ?: return

            val fieldValue = interfaceField.headerFieldValue ?: return

            val module = ModuleUtil.findModuleForPsiElement(header) ?: return

            val interfacePsiClass = DubboUtils.findInterface(module, fieldValue.text) ?: return

            interfacePsiClass.methods
                .forEach {
                    val desc = HttpUtils.getMethodDesc(it)
                    val builder = LookupElementBuilder.create(it.name).withBoldness(true)
                        .withPsiElement(it).withTailText(it.parameterList.text)
                        .withTypeText(it.returnTypeElement?.text + " " + desc)
                    result.addElement(builder)
                }
        }

    }
}