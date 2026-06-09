package org.javamaster.httpclient.utils

import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.intellij.lang.Language
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.putUserData
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.EditorTextField
import org.javamaster.httpclient.action.dashboard.SoftWrapAction
import org.javamaster.httpclient.action.dashboard.view.FoldHeadersAction
import org.javamaster.httpclient.action.dashboard.view.ShowLineNumberAction
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.parser.HttpFile


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
        virtualFile: VirtualFile,
        document: Document,
        isViewer: Boolean,
        project: Project,
        req: Boolean,
        simpleTypeEnum: SimpleTypeEnum?,
        editorList: MutableList<Editor>,
    ): Editor {
        var editor = EditorFactory.getInstance().createEditor(document, project, virtualFile, isViewer)
        editorList.add(editor)

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

    fun createEditor(text: String, fileName: String, project: Project): EditorTextField {
        val lightVirtualFile = LightVirtualFile(fileName, text)
        val file: PsiFile = lightVirtualFile.findPsiFile(project)!!
        val doc = PsiDocumentManager.getInstance(project).getDocument(file)

        return object : EditorTextField(doc, project, file.language.associatedFileType) {

            override fun createEditor(): EditorEx {
                val editor = super.createEditor()
                editor.setVerticalScrollbarVisible(true)
                editor.isOneLineMode = false
                editor.isViewer = true

                val settings = editor.settings
                settings.isLineNumbersShown = true

                return editor
            }

        }

    }

    fun createEditor(text: String, placeholder: String, language: Language, project: Project): EditorTextField {
        val file: PsiFile = PsiFileFactory.getInstance(project).createFileFromText(language, text)
        val doc = PsiDocumentManager.getInstance(project).getDocument(file)

        return object : EditorTextField(doc, project, language.associatedFileType) {

            override fun createEditor(): EditorEx {
                val editor = super.createEditor()
                editor.setVerticalScrollbarVisible(true)
                editor.isOneLineMode = false
                editor.setPlaceholder(placeholder)

                val settings = editor.settings
                settings.isUseSoftWraps = true
                settings.isLineNumbersShown = true

                return editor
            }

        }
    }

}

