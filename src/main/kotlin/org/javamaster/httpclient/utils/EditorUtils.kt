package org.javamaster.httpclient.utils

import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.putUserData
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.LightVirtualFile
import org.javamaster.httpclient.action.dashboard.SoftWrapAction
import org.javamaster.httpclient.action.dashboard.view.FoldHeadersAction
import org.javamaster.httpclient.action.dashboard.view.ShowLineNumberAction
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.utils.VirtualFileUtils.createHttpVirtualFileFromText
import java.nio.charset.StandardCharsets


object EditorUtils {

    fun setEditorFoldHeader(foldHeader: Boolean, editor: Editor) {
        val project = editor.project!!
        val document = editor.document
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
        if (psiFile !is HttpFile) {
            return
        }

        val requestBlocks = psiFile.getRequestBlocks()
        if (requestBlocks.isEmpty()) {
            return
        }

        val header = requestBlocks[0].request?.header ?: return

        val foldingModel = editor.foldingModel

        val foldRegions = FoldingUtil.getFoldRegionsAtOffset(editor, header.textRange.startOffset)
        if (foldRegions.isEmpty()) {
            return
        }

        foldingModel.runBatchFoldingOperation {
            foldRegions[0].isExpanded = !foldHeader
        }
    }

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

            setEditorFoldHeader(FoldHeadersAction.reqFoldHeader, editor)
            document.putUserData(HttpConsts.httpDashboardFoldHeaderKey, FoldHeadersAction.reqFoldHeader)
        } else {
            settings.isUseSoftWraps = SoftWrapAction.resUseSoftWrap
            settings.isLineNumbersShown = ShowLineNumberAction.resShowLineNum

            setEditorFoldHeader(FoldHeadersAction.resFoldHeader, editor)
            document.putUserData(HttpConsts.httpDashboardFoldHeaderKey, FoldHeadersAction.resFoldHeader)
        }

        val component = editor.component

        component.putUserData(HttpConsts.httpDashboardToolbarKey, req)
        component.putUserData(HttpConsts.httpDashboardResTypeKey, simpleTypeEnum)

        val key = if (req) {
            HttpConsts.httpDashboardReqEditorKey
        } else {
            HttpConsts.httpDashboardResEditorKey
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
        isViewer: Boolean,
    ): Editor {
        val editorFactory = EditorFactory.getInstance()
        val fileDocumentManager = FileDocumentManager.getInstance()

        val legalTabName = PathUtils.legalizeFileName(tabName)

        var virtualFile = createHttpVirtualFileFromText(bytes, suffix, project, legalTabName, noLog)

        val psiFile = virtualFile.findPsiFile(project)

        val editor = if (psiFile != null) {
            val document = fileDocumentManager.getDocument(virtualFile)!!
            editorFactory.createEditor(document, project, virtualFile, isViewer)
        } else {
            virtualFile = LightVirtualFile(virtualFile.name)
            virtualFile.charset = StandardCharsets.UTF_8
            virtualFile.setBinaryContent(bytes)

            val document = fileDocumentManager.getDocument(virtualFile)!!
            editorFactory.createEditor(document, project, virtualFile, isViewer)
        }

        editorList.add(editor)

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
        return createEditor(bytes, suffix, project, tabName, editorList, noLog, true)
    }
}

