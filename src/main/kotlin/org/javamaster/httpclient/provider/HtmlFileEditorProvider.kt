package org.javamaster.httpclient.provider

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.key.HttpKey.httpDashboardBinaryBodyKey
import org.javamaster.httpclient.support.HtmlFileEditor

/**
 * @author yudong
 */
class HtmlFileEditorProvider : FileEditorProvider, DumbAware {

    override fun accept(project: Project, virtualFile: VirtualFile): Boolean {
        return virtualFile.getUserData(httpDashboardBinaryBodyKey) == true
    }

    override fun createEditor(project: Project, virtualFile: VirtualFile): FileEditor {
        return HtmlFileEditor(project, virtualFile)
    }

    override fun getEditorTypeId(): String {
        return "htmlEditorTypeId"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR
    }
}
