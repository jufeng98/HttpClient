package org.javamaster.httpclient.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.CollectionFactory
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.completion.support.HttpHeadersDictionary.getHeaderValues
import org.javamaster.httpclient.completion.support.HttpHeadersDictionary.headers
import org.javamaster.httpclient.completion.support.HttpSuffixInsertHandler
import org.javamaster.httpclient.psi.*


class HttpRequestCompletionContributor : CompletionContributor() {
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
            CompletionType.BASIC, PlatformPatterns.psiElement(HttpTypes.HOST_VALUE).withParent(
                HttpHost::class.java
            ),
            HttpMethodsProvider()
        )

        this.extend(
            CompletionType.BASIC, StandardPatterns.or(
                PlatformPatterns.psiElement(HttpTypes.MESSAGE_TEXT),
                PlatformPatterns.psiElement(TokenType.WHITE_SPACE)
                    .afterLeaf(PlatformPatterns.psiElement(HttpTypes.MESSAGE_TEXT))
            ),
            HttpMessageBodySeparatorCompletion()
        )

        this.extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().afterLeafSkipping(
                StandardPatterns.or(
                    PlatformPatterns.psiElement(
                        PsiErrorElement::class.java
                    ),
                    PlatformPatterns.psiElement(TokenType.BAD_CHARACTER)
                ), PlatformPatterns.psiElement(HttpTypes.MESSAGE_BOUNDARY)
            ),
            HttpMessageBodySeparatorOptionsCompletion()
        )
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        super.beforeCompletion(context)

        val psiElement = context.file.findElementAt(context.startOffset)
        if (psiElement != null && HttpPsiUtils.isOfType(psiElement, HttpTypes.FIELD_VALUE)) {
            val startOffset = psiElement.textRange.startOffset
            val separator = psiElement.text.indexOf(",", context.startOffset - startOffset)
            context.replacementOffset = if (separator < 0) psiElement.textRange.endOffset else startOffset + separator
        } else if (!isDummyIdentifierCanExtendMessageBody(psiElement, context)
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
                        context.replacementOffset =
                            getSchemeReplacementOffset(previousElementParent)
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

    private class HttpMethodsProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            if (!isRequestStart(parameters)) return

            result.addElement(
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create((HttpTypes.POST as HttpElementType).name)
                        .withBoldness(true)
                        .withInsertHandler(AddSpaceInsertHandler.INSTANCE), 100.0
                )
            )

            result.addElement(
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create((HttpTypes.GET as HttpElementType).name)
                        .withBoldness(true)
                        .withInsertHandler(AddSpaceInsertHandler.INSTANCE), 100.0
                )
            )

            result.addElement(
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create((HttpTypes.WEBSOCKET as HttpElementType).name)
                        .withBoldness(true)
                        .withInsertHandler(AddSpaceInsertHandler.INSTANCE), 100.0
                )
            )

            result.addElement(
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create((HttpTypes.DUBBO as HttpElementType).name)
                        .withBoldness(true)
                        .withInsertHandler(AddSpaceInsertHandler.INSTANCE), 100.0
                )
            )
        }

        private fun isRequestStart(parameters: CompletionParameters): Boolean {
            val request = PsiTreeUtil.getParentOfType(
                parameters.position,
                HttpRequest::class.java
            )
            val target = PsiTreeUtil.getParentOfType(
                parameters.position,
                HttpRequestTarget::class.java
            )
            if (request != null && target != null) {
                val currentStartOffset = parameters.position.textRange.startOffset
                return request.textRange.startOffset == currentStartOffset
            } else {
                return false
            }
        }
    }

    private class HttpHeaderFieldNamesProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val alreadyAdded = CollectionFactory.createCaseInsensitiveStringSet()

            for (header in headers.values) {
                result.addElement(
                    PrioritizedLookupElement.withPriority(
                        LookupElementBuilder.create(header, header.name)
                            .withStrikeoutness(header.isDeprecated)
                            .withInsertHandler(HttpSuffixInsertHandler.FIELD_SEPARATOR),
                        if (header.isDeprecated) 100.0 else 200.0
                    )
                )
                alreadyAdded.add(header.name)
            }
        }
    }

    private class HttpHeaderFieldValuesProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val header = PsiTreeUtil.getParentOfType(
                CompletionUtil.getOriginalOrSelf(parameters.position),
                HttpHeaderField::class.java
            )
            val headerName = header?.headerFieldName?.text
            if (StringUtil.isEmpty(headerName)) {
                return
            }

            if (headerName.equals("Sec-WebSocket-Protocol", ignoreCase = true)) {
                for (protocol in myWebSocketProtocols) {
                    result.addElement(
                        PrioritizedLookupElement.withPriority(
                            LookupElementBuilder.create(protocol),
                            200.0
                        )
                    )
                }
            }

            for (value in getHeaderValues(headerName!!)) {
                result.addElement(PrioritizedLookupElement.withPriority(LookupElementBuilder.create(value), 200.0))
            }

            result.addElement(
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create(
                        ContentType.MULTIPART_FORM_DATA.mimeType + "; boundary=----WebBoundary"
                    ), 200.0
                )
            )

            result.addElement(
                PrioritizedLookupElement.withPriority(
                    LookupElementBuilder.create(
                        "Content-Disposition"
                    ), 200.0
                )
            )
        }

        companion object {
            private val myWebSocketProtocols = listOf("graphql-ws", "subscriptions-transport-ws", "aws-app-sync")
        }
    }

    private class HttpMessageBodySeparatorCompletion : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val offset = parameters.offset
            val currentLine = parameters.editor.document.getLineNumber(offset)
            val lineStartOffset = parameters.editor.document.getLineStartOffset(currentLine)
            val lineEndOffset = parameters.editor.document.getLineEndOffset(currentLine)
            val text = parameters.editor.document.getText(TextRange.create(lineStartOffset, lineEndOffset))

            if (!text.chars().allMatch { i: Int -> Character.isWhitespace(i.toChar()) || i == 61 }) {
                return
            }

            val insertHandler = InsertHandler { insertionContext: InsertionContext, item: LookupElement ->
                val document = insertionContext.document
                val line = document.getLineNumber(insertionContext.startOffset)
                val newStartOffset = document.getLineStartOffset(line)
                document.replaceString(newStartOffset, insertionContext.tailOffset, item.lookupString)
                insertionContext.editor.caretModel.moveToOffset(newStartOffset + item.lookupString.length)
            }

            result.addElement(LookupElementBuilder.create("=== ").withInsertHandler(insertHandler))
            result.addElement(LookupElementBuilder.create("=== wait-for-server").withInsertHandler(insertHandler))
        }
    }

    private class HttpMessageBodySeparatorOptionsCompletion : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            result.addElement(
                LookupElementBuilder.create("wait-for-server")
                    .withInsertHandler { insertionContext: InsertionContext, _: LookupElement? ->
                        val document = insertionContext.editor.document
                        val line = document.getLineNumber(insertionContext.startOffset)
                        val lastEqualSign = StringUtil.lastIndexOf(
                            document.charsSequence, '=',
                            document.getLineStartOffset(line), insertionContext.startOffset
                        )
                        if (lastEqualSign != -1) {
                            document.replaceString(
                                lastEqualSign + 1, insertionContext.tailOffset,
                                " wait-for-server"
                            )
                        }
                    })
        }
    }

    private val identifierPredecessor = TokenSet.create(
        HttpTypes.IDENTIFIER,
        HttpTypes.START_VARIABLE_BRACE,
    )

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
            if (context.startOffset > 0) {
                val prevElement = context.file.findElementAt(context.startOffset - 1)
                if (prevElement != null && HttpPsiUtils.isOfTypes(prevElement, identifierPredecessor)) {
                    return prevElement
                }
            }

            return null
        }
    }


}