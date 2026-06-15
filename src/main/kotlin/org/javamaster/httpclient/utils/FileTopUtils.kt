package org.javamaster.httpclient.utils

import com.intellij.json.JsonFileType
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application
import com.intellij.util.concurrency.AppExecutorUtil
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.env.EnvFileService.Companion.getService
import org.javamaster.httpclient.logger.HttpRequestLogger.logInfo
import org.javamaster.httpclient.ui.HttpEditorTopForm

/**
 * @author yudong
 */
object FileTopUtils {
    @Volatile
    private var jsonAssociated = false

    fun initFileStatus(source: FileEditorManager, file: VirtualFile) {
        if (file.fileType !is HttpFileType) {
            return
        }

        val project = source.project

        val fileEditors = source.getAllEditors(file)
        for (fileEditor in fileEditors) {
            if (HttpUtils.isFileInHistoryDir(file, project)) {
                val textEditor = fileEditor as TextEditor
                textEditor.editor.document.setReadOnly(true)
                continue
            }

            val module = ModuleUtil.findModuleForFile(file, project)

            if (jsonAssociated) {
                initTopForm(source, file, module, fileEditor, project)
                continue
            }

            val fileTypeManagerEx = FileTypeManagerEx.getInstanceEx()

            val jsonFileType = JsonFileType.INSTANCE
            val jsonExtension = jsonFileType.defaultExtension

            val currentType = fileTypeManagerEx.getFileTypeByExtension(jsonExtension)
            if (currentType === jsonFileType) {
                jsonAssociated = true

                initTopForm(source, file, module, fileEditor, project)
                continue
            }

            if (currentType != UnknownFileType.INSTANCE && currentType != PlainTextFileType.INSTANCE) {
                initTopForm(source, file, module, fileEditor, project)
                continue
            }

            @Suppress("DEPRECATION")
            runInEdt(ModalityState.NON_MODAL) {
                application.runWriteAction {
                    jsonAssociated = true

                    fileTypeManagerEx.associateExtension(jsonFileType, jsonExtension)
                    logInfo("The json suffix file has been associated with the $jsonFileType")
                }

                application.executeOnPooledThread { initTopForm(source, file, module, fileEditor, project) }
            }
        }
    }

    fun clearFileStatus(source: FileEditorManager, file: VirtualFile) {
        for (fileEditor in source.getAllEditors(file)) {
            val httpEditorTopForm = fileEditor.getUserData(HttpEditorTopForm.KEY)
            if (httpEditorTopForm == null) {
                continue
            }

            fileEditor.putUserData(HttpEditorTopForm.KEY, null)

            runInEdt {
                source.removeTopComponent(fileEditor, httpEditorTopForm.mainPanel)
            }
        }
    }

    private fun initTopForm(
        source: FileEditorManager,
        file: VirtualFile,
        module: Module?,
        fileEditor: FileEditor,
        project: Project,
    ) {
        if (fileEditor.getUserData(HttpEditorTopForm.KEY) != null) return

        val parentPath = file.parent?.path ?: return

        ReadAction
            .nonBlocking<MutableSet<String>> { getService(project).getPresetEnvSet(parentPath) }
            .expireWhen { project.isDisposed }
            .finishOnUiThread(ModalityState.defaultModalityState()) {
                val httpEditorTopForm = HttpEditorTopForm(file, module, fileEditor)

                httpEditorTopForm.initEnvCombo(it)

                fileEditor.putUserData(HttpEditorTopForm.KEY, httpEditorTopForm)

                source.addTopComponent(fileEditor, httpEditorTopForm.mainPanel)
            }
            .submit(AppExecutorUtil.getAppExecutorService())
    }

}