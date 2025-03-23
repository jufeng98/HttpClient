package org.javamaster.httpclient.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange

/**
 * @author yudong
 */
object VariableAnnotator {

    fun annotateVariable(builtin: Boolean, range: TextRange, holder: AnnotationHolder) {
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

}
