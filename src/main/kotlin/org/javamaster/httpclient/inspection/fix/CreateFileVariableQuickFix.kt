package org.javamaster.httpclient.inspection.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile

/**
 * @author yudong
 */
class CreateFileVariableQuickFix(private val variableName: String) : LocalQuickFix, PriorityAction {

    override fun getFamilyName(): String {
        return NlsBundle.nls("unsolved.file.variable")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        createGlobalVariable(project)
    }

    private fun createGlobalVariable(project: Project) {
        if (!ApplicationManager.getApplication().isDispatchThread) {
            return
        }

        val elementNew = HttpFile.createFileVariableAndInsert(variableName, "ju", project)

        (elementNew?.lastChild as? Navigatable?)?.navigate(true)
    }

    override fun getPriority(): PriorityAction.Priority {
        return PriorityAction.Priority.NORMAL
    }

}
