package org.javamaster.httpclient.inspection.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.elementType
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.CookieRecord
import org.javamaster.httpclient.psi.CookieTypes

/**
 * @author yudong
 */
object DelCookieQuickFix : LocalQuickFix, PriorityAction {

    override fun getFamilyName(): String {
        return NlsBundle.nls("cookie.del")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val cookieRecord = descriptor.psiElement as CookieRecord

        val prevSibling = cookieRecord.prevSibling
        val nextSibling = cookieRecord.nextSibling

        cookieRecord.delete()

        if (nextSibling.elementType == CookieTypes.NEW_LINE) {
            nextSibling.delete()
        } else if (prevSibling.elementType == CookieTypes.NEW_LINE) {
            prevSibling.delete()
        }
    }

    override fun getPriority(): PriorityAction.Priority {
        return PriorityAction.Priority.LOW
    }

}
