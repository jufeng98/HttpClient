package org.javamaster.httpclient.completion.provider

import com.google.common.net.HttpHeaders
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.completion.support.HttpHeadersDictionary
import org.javamaster.httpclient.completion.support.HttpHeadersDictionary.encodingValues
import org.javamaster.httpclient.completion.support.HttpHeadersDictionary.predefinedMimeVariants
import org.javamaster.httpclient.psi.HttpHeader
import org.javamaster.httpclient.psi.HttpHeaderField
import org.javamaster.httpclient.utils.DubboUtils

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

        if (headerName.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)
            || headerName.equals(HttpHeaders.ACCEPT, ignoreCase = true)
        ) {
            for (value in predefinedMimeVariants) {
                result.addElement(PrioritizedLookupElement.withPriority(LookupElementBuilder.create(value), 200.0))
            }
            return
        }

        if (headerName.equals(HttpHeaders.ACCEPT_ENCODING, ignoreCase = true)) {
            for (value in encodingValues) {
                result.addElement(PrioritizedLookupElement.withPriority(LookupElementBuilder.create(value), 200.0))
            }
            return
        }

        if (headerName.equals(HttpHeaders.REFERRER_POLICY, ignoreCase = true)) {
            for (value in HttpHeadersDictionary.referrerPolicyValues) {
                result.addElement(PrioritizedLookupElement.withPriority(LookupElementBuilder.create(value), 200.0))
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
            interfacePsiClass.methods.forEach {
                val builder = LookupElementBuilder.create(it.name).withBoldness(true)
                    .withPsiElement(it).withTailText(it.parameterList.text)
                    .withTypeText(it.returnTypeElement?.text)
                result.addElement(builder)
            }
            return
        }

        if (headerName.equals(HttpHeaders.SEC_WEBSOCKET_PROTOCOL, ignoreCase = true)) {
            for (protocol in HttpHeadersDictionary.secWebsocketProtocolValues) {
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(protocol),
                        200.0
                    )
                )
            }
        }
    }
}