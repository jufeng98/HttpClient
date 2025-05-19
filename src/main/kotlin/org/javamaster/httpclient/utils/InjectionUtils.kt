package org.javamaster.httpclient.utils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

object InjectionUtils {

    fun innerRange(context: PsiElement): TextRange? {
        val textRange = context.textRange
        val textRangeTmp = textRange.shiftLeft(textRange.startOffset)
        if (textRangeTmp.endOffset == 0) {
            return null
        }

        return textRangeTmp
    }

}