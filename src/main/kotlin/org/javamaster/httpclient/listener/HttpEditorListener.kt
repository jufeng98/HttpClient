package org.javamaster.httpclient.listener

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * 为 http 文件添加顶部工具栏
 *
 * @author yudong
 */
class HttpEditorListener : FileEditorManagerListener {
    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        if (file.fileType !is HttpFileType) {
            return
        }

        val fileEditor = source.getSelectedEditor(file) ?: return
        val project = source.project

        val module = ModuleUtil.findModuleForFile(file, project) ?: return

        val httpEditorTopForm = HttpEditorTopForm()

        try {
            httpEditorTopForm.initEnvCombo(module, file.parent.path)
        } catch (e: Exception) {
            NotifyUtil.notifyError(project, e.message)
        }

        fileEditor.putUserData(HttpEditorTopForm.KEY, httpEditorTopForm)

        source.addTopComponent(fileEditor, httpEditorTopForm.mainPanel)
    }

}
