package org.javamaster.httpclient.manipulator

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtil
import com.intellij.refactoring.rename.RenameHandler
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.refactoring.util.CommonRefactoringUtil
import org.javamaster.httpclient.psi.HttpGlobalVariableName


/**
 * @author yudong
 */
class HttpRenameHandler : RenameHandler {

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
        val offset = editor?.caretModel?.currentCaret?.offset ?: return

        val psiElement = file?.findElementAt(offset) ?: return

        val parent = psiElement.parent
        val processor = RenamePsiElementProcessor.forPsiElement(parent)

        // 以后可以考虑实现成类似重命名类名那样的效果,不弹出弹窗,这样用户体验更好
        processor.createDialog(project, parent, parent, editor).show()
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {

    }

    override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return false
        val psiFile = PsiUtil.getPsiFile(editor.project!!, editor.virtualFile)

        val psiElement = CommonRefactoringUtil.getElementAtCaret(editor, psiFile)

        return psiElement?.parent is HttpGlobalVariableName
    }

}
