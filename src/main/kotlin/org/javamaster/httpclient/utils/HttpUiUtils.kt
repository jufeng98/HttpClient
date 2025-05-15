package org.javamaster.httpclient.utils

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.putUserData
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.action.dashboard.DashboardBaseAction
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.utils.VirtualFileUtils.createHttpVirtualFileFromText
import javax.swing.JComponent

object HttpUiUtils {

    fun createEditorCompo(
        bytes: ByteArray,
        suffix: String,
        project: Project,
        tabName: String,
        editorList: MutableList<Editor>,
        req: Boolean,
        simpleTypeEnum: SimpleTypeEnum?,
    ): JComponent {
        val editor = createEditor(bytes, suffix, project, tabName, editorList)

        val component = editor.component

        component.putUserData(DashboardBaseAction.httpDashboardToolbarKey, req)
        component.putUserData(DashboardBaseAction.httpDashboardResTypeKey, simpleTypeEnum)

        val key = if (req) {
            DashboardBaseAction.httpDashboardReqEditorKey
        } else {
            DashboardBaseAction.httpDashboardResEditorKey
        }

        component.putUserData(key, editor)

        return component
    }

    fun createEditor(
        bytes: ByteArray,
        suffix: String,
        project: Project,
        tabName: String,
        editorList: MutableList<Editor>,
    ): Editor {
        val virtualFile = createHttpVirtualFileFromText(bytes, suffix, project, tabName)

        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val psiFile = PsiUtil.getPsiFile(project, virtualFile)

        val document = psiDocumentManager.getDocument(psiFile)

        val editorFactory = EditorFactory.getInstance()
        val editor = editorFactory.createEditor(document!!, project, virtualFile, true)
        editorList.add(editor)

        return editor
    }

}

