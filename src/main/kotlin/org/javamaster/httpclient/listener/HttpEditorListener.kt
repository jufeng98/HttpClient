package org.javamaster.httpclient.listener

import com.intellij.json.JsonFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.module.Module
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

        val fileTypeManagerEx = FileTypeManagerEx.getInstanceEx()
        val jsonFileType = JsonFileType.INSTANCE
        val extension = fileTypeManagerEx.getFileTypeByExtension(jsonFileType.defaultExtension)
        if (extension !== jsonFileType) {
            val application = ApplicationManager.getApplication()
            application.invokeLater({
                application.runWriteAction {
                    fileTypeManagerEx.associateExtension(jsonFileType, JsonFileType.DEFAULT_EXTENSION)

                    initTopForm(source, file, module, fileEditor)
                }
            }, getState())
        } else {
            initTopForm(source, file, module, fileEditor)
        }
    }

    private fun initTopForm(source: FileEditorManager, file: VirtualFile, module: Module, fileEditor: FileEditor) {
        val httpEditorTopForm = HttpEditorTopForm(file, module)

        try {
            httpEditorTopForm.initEnvCombo()
        } catch (e: Exception) {
            e.printStackTrace()
            NotifyUtil.notifyError(module.project, e.message)
        }

        fileEditor.putUserData(HttpEditorTopForm.KEY, httpEditorTopForm)

        source.addTopComponent(fileEditor, httpEditorTopForm.mainPanel)
    }

    private fun getState(): ModalityState {
        // 兼容下旧版
        val cls = ModalityState::class.java
        try {
            val method = cls.getDeclaredMethod("nonModal")
            method.isAccessible = true
            return method.invoke(null) as ModalityState
        } catch (e: Exception) {
            val field = cls.getDeclaredField("NON_MODAL")
            field.isAccessible = true
            return field.get(null) as ModalityState
        }
    }

}
