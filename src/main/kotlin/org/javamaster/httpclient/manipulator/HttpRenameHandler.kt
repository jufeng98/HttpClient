package org.javamaster.httpclient.manipulator

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.PsiElementRenameHandler
import com.intellij.refactoring.rename.RenameHandler
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.jsPlugin.JsFacade


/**
 * @author yudong
 */
class HttpRenameHandler : RenameHandler {

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext) {
        val offset = editor?.caretModel?.currentCaret?.offset ?: return

        val psiElement = file?.findElementAt(offset) ?: return

        val elementToRename = PsiElementRenameHandler.getElement(dataContext) ?: return

        InjectedJsRenameDialog(project, elementToRename, psiElement.parent, editor).show()
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {

    }

    override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return false
        val virtualFile = editor.virtualFile ?: return false

        if (virtualFile.fileType !is HttpFileType) {
            return false
        }

        val elementToRename = PsiElementRenameHandler.getElement(dataContext) ?: return false

        return JsFacade.referenceToJsVariable(elementToRename)
    }

}
