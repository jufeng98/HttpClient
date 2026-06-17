package org.javamaster.httpclient.annotator

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.apache.commons.lang3.time.DateUtils
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.inspection.fix.AdjustCookieQuickFix
import org.javamaster.httpclient.inspection.fix.DelCookieQuickFix
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.CookieRecord
import java.util.*

/**
 * @author yudong
 */
class CookiesAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is CookieRecord) return

        val value = element.date.text
        if (value == "-1") {
            holder.newAnnotation(HighlightSeverity.INFORMATION, "Cookie never expired")
                .range(element.date)
                .create()
            return
        }

        val date: Date
        try {
            date = DateUtils.parseDate(value, Locale.ENGLISH, HttpConsts.JS_DATE_PATTERN)
        } catch (_: Exception) {
            holder.newAnnotation(HighlightSeverity.ERROR, nls("cookie.date.error"))
                .range(element.date)
                .create()
            return
        }

        if (date >= Date()) return

        val manager = InspectionManager.getInstance(element.project)

        val descriptor = manager.createProblemDescriptor(
            element, element, nls("cookie.adjust"), ProblemHighlightType.WARNING, false
        )

        holder.newAnnotation(HighlightSeverity.WARNING, nls("cookie.expired"))
            .newLocalQuickFix(AdjustCookieQuickFix, descriptor)
            .registerFix()
            .newLocalQuickFix(DelCookieQuickFix, descriptor)
            .registerFix()
            .create()
    }

}
