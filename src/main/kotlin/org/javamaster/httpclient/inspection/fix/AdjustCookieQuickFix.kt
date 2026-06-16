package org.javamaster.httpclient.inspection.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.time.DateUtils
import org.javamaster.httpclient.factory.CookiePsiFactory
import org.javamaster.httpclient.js.support.jsObject.Cookie
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.CookieRecord
import java.util.*

/**
 * @author yudong
 */
object AdjustCookieQuickFix : LocalQuickFix, PriorityAction {

    override fun getFamilyName(): String {
        return NlsBundle.nls("cookie.adjust")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val cookieRecord = descriptor.psiElement as CookieRecord

        val cookie = Cookie(
            cookieRecord.domain.text, cookieRecord.path.text, cookieRecord.nameCk.text, cookieRecord.value.text,
            DateUtils.addMonths(Date(), 3).time, false, false
        )

        val cookieRecordNew = CookiePsiFactory.createRecord(project, cookie)

        cookieRecord.replace(cookieRecordNew)
    }

    override fun getPriority(): PriorityAction.Priority {
        return PriorityAction.Priority.HIGH
    }

}
