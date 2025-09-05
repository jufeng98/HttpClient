package org.javamaster.httpclient.utils

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.putUserData
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.testFramework.LightVirtualFile
import org.javamaster.httpclient.action.dashboard.DashboardBaseAction
import org.javamaster.httpclient.action.dashboard.SoftWrapAction
import org.javamaster.httpclient.action.dashboard.view.FoldHeadersAction
import org.javamaster.httpclient.action.dashboard.view.ShowLineNumberAction
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.utils.VirtualFileUtils.createHttpVirtualFileFromText
import java.nio.charset.StandardCharsets


object EditorUtils {

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
        val editorFactory = EditorFactory.getInstance()
        val fileDocumentManager = FileDocumentManager.getInstance()

        val virtualFile = createHttpVirtualFileFromText(bytes, suffix, project, tabName, noLog)

        val psiFile = virtualFile.findPsiFile(project)

        val editor = if (psiFile != null) {
            val document = fileDocumentManager.getDocument(virtualFile)!!
            editorFactory.createEditor(document, project, virtualFile, true)
        } else {
            val lightVirtualFile = LightVirtualFile(virtualFile.name)
            lightVirtualFile.charset = StandardCharsets.UTF_8
            lightVirtualFile.setBinaryContent(bytes)

            val document = fileDocumentManager.getDocument(lightVirtualFile)!!
            editorFactory.createEditor(document, project, lightVirtualFile, true)
        }

        editorList.add(editor)

        return editor
    }

}

