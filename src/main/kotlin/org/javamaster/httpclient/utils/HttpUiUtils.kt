package org.javamaster.httpclient.utils

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.putUserData
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.action.dashboard.DashboardBaseAction
import org.javamaster.httpclient.action.dashboard.SoftWrapAction
import org.javamaster.httpclient.action.dashboard.view.FoldHeadersAction
import org.javamaster.httpclient.action.dashboard.view.ShowLineNumberAction
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.utils.VirtualFileUtils.createHttpVirtualFileFromText

object HttpUiUtils {

    fun createEditor(
        bytes: ByteArray,
        suffix: String,
        project: Project,
        tabName: String,
        editorList: MutableList<Editor>,
        req: Boolean,
        simpleTypeEnum: SimpleTypeEnum?,
        noLog: Boolean,
    ): Editor {
        val editor = createEditor(bytes, suffix, project, tabName, editorList, noLog)
        val document = editor.document

        val settings = editor.settings
        if (req) {
            settings.isUseSoftWraps = SoftWrapAction.reqUseSoftWrap
            settings.isLineNumbersShown = ShowLineNumberAction.reqShowLineNum

            FoldHeadersAction.setEditorFoldHeader(FoldHeadersAction.reqFoldHeader, editor)
            document.putUserData(FoldHeadersAction.httpDashboardFoldHeaderKey, FoldHeadersAction.reqFoldHeader)
        } else {
            settings.isUseSoftWraps = SoftWrapAction.resUseSoftWrap
            settings.isLineNumbersShown = ShowLineNumberAction.resShowLineNum

            FoldHeadersAction.setEditorFoldHeader(FoldHeadersAction.resFoldHeader, editor)
            document.putUserData(FoldHeadersAction.httpDashboardFoldHeaderKey, FoldHeadersAction.resFoldHeader)
        }

        val component = editor.component

        component.putUserData(DashboardBaseAction.httpDashboardToolbarKey, req)
        component.putUserData(DashboardBaseAction.httpDashboardResTypeKey, simpleTypeEnum)

        val key = if (req) {
            DashboardBaseAction.httpDashboardReqEditorKey
        } else {
            DashboardBaseAction.httpDashboardResEditorKey
        }

        component.putUserData(key, editor)

        return editor
    }

    fun createEditor(
        bytes: ByteArray,
        suffix: String,
        project: Project,
        tabName: String,
        editorList: MutableList<Editor>,
        noLog: Boolean,
    ): Editor {
        val virtualFile = createHttpVirtualFileFromText(bytes, suffix, project, tabName, noLog)

        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val psiFile = PsiUtil.getPsiFile(project, virtualFile)

        val document = psiDocumentManager.getDocument(psiFile)

        val editorFactory = EditorFactory.getInstance()
        val editor = editorFactory.createEditor(document!!, project, virtualFile, true)
        editorList.add(editor)

        return editor
    }

}

