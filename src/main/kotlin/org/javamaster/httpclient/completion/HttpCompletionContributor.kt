package org.javamaster.httpclient.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.completion.provider.HttpDirectionNameCompletionProvider
import org.javamaster.httpclient.completion.provider.HttpHeaderFieldNamesProvider
import org.javamaster.httpclient.completion.provider.HttpHeaderFieldValuesProvider
import org.javamaster.httpclient.completion.provider.HttpMethodsProvider
import org.javamaster.httpclient.psi.*

/**
 * @author yudong
 */
class HttpCompletionContributor : CompletionContributor() {
    private val identifierPredecessor = TokenSet.create(
        HttpTypes.IDENTIFIER,
        HttpTypes.START_VARIABLE_BRACE,
    )

    init {
        this.extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
                HttpHeaderFieldName::class.java
            ),
            HttpHeaderFieldNamesProvider()
        )

        this.extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
                HttpHeaderFieldValue::class.java
            ),
            HttpHeaderFieldValuesProvider()
        )

        this.extend(
            CompletionType.BASIC, PlatformPatterns.psiElement(HttpTypes.REQUEST_METHOD),
            HttpMethodsProvider()
        )

        this.extend(
            CompletionType.BASIC, PlatformPatterns.psiElement(HttpTypes.DIRECTION_NAME_PART),
            HttpDirectionNameCompletionProvider()
        )
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        super.beforeCompletion(context)

        val psiElement = context.file.findElementAt(context.startOffset)
        if (psiElement != null && HttpPsiUtils.isOfType(psiElement, HttpTypes.FIELD_VALUE)) {
            val startOffset = psiElement.textRange.startOffset
            val separator = psiElement.text.indexOf(",", context.startOffset - startOffset)
            context.replacementOffset = if (separator < 0) psiElement.textRange.endOffset else startOffset + separator
        } else if (
            !isDummyIdentifierCanExtendMessageBody(psiElement, context)
            && !isBeforePossiblePreRequestHandler(context)
        ) {
            val parent = psiElement?.parent
            if (parent is HttpSchema) {
                context.replacementOffset = getSchemeReplacementOffset(parent)
            }

            if (parent is HttpHeaderFieldName || parent is HttpHost) {
                context.replacementOffset = parent.textRange.endOffset
            }

            if (parent is HttpRequestTarget) {
                context.replacementOffset = parent.getTextRange().startOffset
                if (context.startOffset > 0) {
                    val previousElement = context.file.findElementAt(context.startOffset - 1)
                    val previousElementParent = previousElement?.parent
                    if (previousElementParent is HttpSchema) {
                        context.replacementOffset = getSchemeReplacementOffset(previousElementParent)
                    }
                }
            } else {
                val toReplace = getReplacedIdentifier(context, parent)
                if (toReplace != null) {
                    context.replacementOffset = toReplace.textRange.endOffset
                }
            }
        } else {
            context.dummyIdentifier = ""
        }
    }

    private fun isDummyIdentifierCanExtendMessageBody(
        element: PsiElement?,
        context: CompletionInitializationContext,
    ): Boolean {
        if (element !is PsiWhiteSpace) {
            return false
        }

        val prevLeaf = PsiTreeUtil.prevLeaf(element)
        if (prevLeaf != null && HttpPsiUtils.isOfType(prevLeaf, HttpTypes.MESSAGE_TEXT)) {
            val document = context.editor.document
            return document.getLineNumber(prevLeaf.textRange.endOffset) != document.getLineNumber(context.startOffset)
        }

        return false
    }

    private fun isBeforePossiblePreRequestHandler(context: CompletionInitializationContext): Boolean {
        val preHandler = PsiTreeUtil.findElementOfClassAtOffset(
            context.file, context.startOffset,
            HttpPreRequestHandler::class.java, false
        )
        if (preHandler != null && preHandler.textRange.startOffset == context.startOffset) {
            return true
        }

        val document = context.editor.document
        var currentIndex = context.startOffset
        val sequence = document.charsSequence
        while (currentIndex < sequence.length && !StringUtil.isLineBreak(sequence[currentIndex]) && sequence[currentIndex] != '<') {
            ++currentIndex
        }

        if (currentIndex <= sequence.length - 1 && sequence[currentIndex] == '<') {
            val possibleOpenBracePosition = StringUtil.skipWhitespaceForward(sequence, currentIndex + 1)
            return possibleOpenBracePosition != sequence.length && StringUtil.startsWith(
                sequence,
                possibleOpenBracePosition,
                "{%"
            )
        } else {
            return false
        }
    }

    private fun getSchemeReplacementOffset(scheme: PsiElement): Int {
        val possibleSeparator = scheme.nextSibling
        if (possibleSeparator != null && HttpPsiUtils.isOfType(
                possibleSeparator,
                HttpTypes.MESSAGE_BOUNDARY
            )
        ) {
            val possibleHost = possibleSeparator.nextSibling
            return if (possibleHost != null && HttpPsiUtils.isOfType(
                    possibleHost,
                    HttpTypes.HOST
                )
            ) possibleHost.textRange.endOffset else possibleSeparator.textRange.endOffset
        } else {
            return scheme.textRange.endOffset
        }
    }

    private fun getReplacedIdentifier(
        context: CompletionInitializationContext,
        parent: PsiElement?,
    ): PsiElement? {
        if (parent is HttpVariable) {
            val toReplace = parent.getFirstChild()
            return toReplace ?: parent.getFirstChild()
        } else {
            if (context.startOffset <= 0) return null

            val prevElement = context.file.findElementAt(context.startOffset - 1)

            if (prevElement != null && HttpPsiUtils.isOfTypes(prevElement, identifierPredecessor)) {
                return prevElement
            }

            return null
        }
    }


}