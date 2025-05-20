package org.javamaster.httpclient.action.dashboard

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.nls.NlsBundle.nls

/**
 * @author yudong
 */
class PreviewFileAction(private val resBodyFile: VirtualFile) :
    DashboardBaseAction(nls("preview"), AllIcons.Actions.Preview) {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = getHttpEditor(e)
        val project = editor.project!!

        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.openFile(resBodyFile, true)
    }

}
