package org.javamaster.httpclient.inspection.fix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpGlobalVariable

/**
 * @author yudong
 */
class CreateFileVariableQuickFix(private val variableName: String) : LocalQuickFix,PriorityAction {

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

        val textEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val httpFile = PsiUtil.getPsiFile(project, textEditor.virtualFile) as HttpFile

        val txt = "@$variableName = \n"

        val psiFileFactory = PsiFileFactory.getInstance(project)

        val tmpFile = psiFileFactory.createFileFromText("dummy.http", HttpLanguage.INSTANCE, txt) as HttpFile
        val newGlobalVariable = PsiTreeUtil.findChildOfType(tmpFile, HttpGlobalVariable::class.java)!!

        val directionComments = httpFile.getDirectionComments()
        val globalHandler = httpFile.getGlobalHandler()

        val elementCopy = if (directionComments.isNotEmpty()) {
            httpFile.addAfter(newGlobalVariable, directionComments.last().nextSibling)
        } else if (globalHandler != null) {
            httpFile.addAfter(newGlobalVariable, globalHandler)
        } else {
            httpFile.addBefore(newGlobalVariable, httpFile.firstChild)
        }

        val whitespace = newGlobalVariable.nextSibling
        elementCopy.add(whitespace)

        val cr = whitespace.nextSibling
        elementCopy.add(cr)

        (elementCopy.lastChild as Navigatable).navigate(true)
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return

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
