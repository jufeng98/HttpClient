package org.javamaster.httpclient.inspection.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class CreateFileVariableQuickFix(private val variableName: String) : LocalQuickFix, PriorityAction {

    override fun getFamilyName(): String {
        return NlsBundle.nls("unsolved.global.variable")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        createGlobalVariable(project)
    }

    private fun createGlobalVariable(project: Project) {
        if (!ApplicationManager.getApplication().isDispatchThread) {
            return
        }

        val elementCopy = HttpUtils.createGlobalVariableAndInsert(variableName, "", project) ?: return

        (elementCopy.lastChild as Navigatable).navigate(true)
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return

        val textEditor = FileEditorManager.getInstance(project).selectedTextEditor!!
        val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile

        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(httpFile) ?: return

        documentManager.doPostponedOperationsAndUnblockDocument(document)

        val caretModel = editor.caretModel
        val offset = caretModel.offset

        // Move the cursor to the value
        document.insertString(offset, " ")
        caretModel.moveToOffset(offset + 1)
    }

    override fun getPriority(): PriorityAction.Priority {
        return PriorityAction.Priority.NORMAL
    }

}
