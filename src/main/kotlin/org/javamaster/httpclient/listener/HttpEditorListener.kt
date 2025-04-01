package org.javamaster.httpclient.listener

import com.intellij.json.JsonFileType
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.background.HttpBackground
import org.javamaster.httpclient.env.EnvFileService.Companion.getService
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * Add a top toolbar to the http file
 *
 * @author yudong
 */
class HttpEditorListener : FileEditorManagerListener {

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        if (file.fileType !is HttpFileType) {
            return
        }

        val fileEditor = source.getSelectedEditor(file)
        if (fileEditor == null) {
            System.err.println("Can't find file editor for ${file.path}")
            return
        }

        application.executeOnPooledThread {
            val project = source.project
            val module = ModuleUtil.findModuleForFile(file, project)
            val fileTypeManagerEx = FileTypeManagerEx.getInstanceEx()

            val jsonFileType = JsonFileType.INSTANCE
            val jsonExtension = jsonFileType.defaultExtension

            val extension = fileTypeManagerEx.getFileTypeByExtension(jsonExtension)
            if (extension === jsonFileType) {
                initTopForm(source, file, module, project, fileEditor)
                return@executeOnPooledThread
            }

            runInEdt(getState()) {
                runWriteAction {
                    fileTypeManagerEx.associateExtension(jsonFileType, jsonExtension)
                    println("The json suffix file has been associated with the $jsonFileType")

                    initTopForm(source, file, module, project, fileEditor)
                }
            }
        }
    }

    private fun initTopForm(
        source: FileEditorManager,
        file: VirtualFile,
        module: Module?,
        project: Project,
        fileEditor: FileEditor,
    ) {
        HttpBackground
            .runInBackgroundReadActionAsync {
                val envFileService = getService(source.project)
                envFileService.getPresetEnvSet(file.parent.path)
            }
            .finishOnUiThread {
                val httpEditorTopForm = HttpEditorTopForm(file, module, project)

                httpEditorTopForm.initEnvCombo(it)

                fileEditor.putUserData(HttpEditorTopForm.KEY, httpEditorTopForm)

                source.addTopComponent(fileEditor, httpEditorTopForm.mainPanel)
            }
            .exceptionallyOnUiThread {
                NotifyUtil.notifyError(source.project, it.message)
            }
    }

    private fun getState(): ModalityState {
        // Compatible with older versions
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
