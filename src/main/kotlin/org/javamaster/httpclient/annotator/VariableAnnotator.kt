package org.javamaster.httpclient.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import org.javamaster.httpclient.psi.HttpVariableArg

/**
 * @author yudong
 */
object VariableAnnotator {

    fun annotateVariableName(builtin: Boolean, range: TextRange, holder: AnnotationHolder) {
        if (range.startOffset == range.endOffset) {
            return
        }

        val tip = if (builtin) {
            "Builtin variable"
        } else {
            "User defined variable"
        }

        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(range)
            .tooltip(tip)
            .textAttributes(DefaultLanguageHighlighterColors.METADATA)
            .create()
    }

    fun annotateVariableArg(variableArg: HttpVariableArg, range: TextRange, holder: AnnotationHolder) {
        if (range.startOffset == range.endOffset) {
            return
        }

        val textAttributes = if (variableArg.string != null) {
            DefaultLanguageHighlighterColors.STRING
        } else {
            DefaultLanguageHighlighterColors.NUMBER
        }

        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(range)
            .textAttributes(textAttributes)
            .create()
    }

    fun annotateRequestName(range: TextRange, holder: AnnotationHolder) {
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(TextRange(range.startOffset, range.startOffset + 3))
            .textAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT)
            .create()
    }

}
