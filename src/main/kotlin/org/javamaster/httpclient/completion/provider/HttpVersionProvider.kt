package org.javamaster.httpclient.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.psi.HttpRequestTarget

/**
 * @author yudong
 */
class HttpVersionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet,
    ) {
        val position = parameters.position
        val requestBlock = PsiTreeUtil.getParentOfType(position, HttpRequestBlock::class.java)
        if (requestBlock == null) return

        val request = requestBlock.request ?: return

        val whitespace = request.nextSibling
        if (whitespace !is PsiWhiteSpace) {
            return
        }

        val last = request.children.last()
        if (last !is HttpRequestTarget) {
            return
        }

        val newPrefix = getTextBetweenSpaceAndPosition(whitespace, position, parameters.offset)

        val newResult = result.withPrefixMatcher(newPrefix)

        newResult.addElement(LookupElementBuilder.create("HTTP/2"))
        newResult.addElement(LookupElementBuilder.create("HTTP/1.1"))
    }

    private fun getTextBetweenSpaceAndPosition(
        whiteSpace: PsiWhiteSpace,
        position: PsiElement,
        offsetInFile: Int,
    ): String {
        var txt = ""
        val psiFile = position.containingFile

        var endOffset = offsetInFile - 1
        var i = whiteSpace.startOffset
        while (i++ < endOffset) {
            val elementAt = psiFile.findElementAt(i)
            txt += elementAt?.text ?: ""
        }

        return txt
    }
}