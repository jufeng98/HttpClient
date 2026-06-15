package org.javamaster.httpclient.inspection.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.EnvFileUtils

/**
 * @author yudong
 */
class CreateEnvVariableQuickFix(
    private val isPrivate: Boolean,
    private val variableName: String,
    private val priority: PriorityAction.Priority,
) : LocalQuickFix,
    PriorityAction {

    override fun getFamilyName(): String {
        val tip = if (isPrivate) "private" else ""
        return NlsBundle.nls("unsolved.variable", tip)
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        EnvFileUtils.createJsonProperty(project, variableName, isPrivate)
    }

    override fun getPriority(): PriorityAction.Priority {
        return priority
    }
}
