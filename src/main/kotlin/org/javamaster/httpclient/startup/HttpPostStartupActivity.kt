package org.javamaster.httpclient.startup

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
import com.intellij.openapi.startup.ProjectActivity
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
class HttpPostStartupActivity : FileEditorManagerListener, ProjectActivity {

    override suspend fun execute(project: Project) {
        val fileEditorManager = FileEditorManager.getInstance(project)
        fileEditorManager.openFiles.forEach {
            fileOpened(fileEditorManager, it)
        }

        project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this)
    }

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
                initTopForm(source, file, module, fileEditor)
                return@executeOnPooledThread
            }

            @Suppress("DEPRECATION")
            runInEdt(ModalityState.NON_MODAL) {
                runWriteAction {
                    fileTypeManagerEx.associateExtension(jsonFileType, jsonExtension)
                    println("The json suffix file has been associated with the $jsonFileType")

                    initTopForm(source, file, module, fileEditor)
                }
            }
        }
    }

    private fun initTopForm(
        source: FileEditorManager,
        file: VirtualFile,
        module: Module?,
        fileEditor: FileEditor,
    ) {
        HttpBackground
            .runInBackgroundReadActionAsync {
                val envFileService = getService(source.project)
                val path = file.parent?.path ?: return@runInBackgroundReadActionAsync null

                envFileService.getPresetEnvSet(path)
            }
            .finishOnUiThread {
                if (it == null) {
                    return@finishOnUiThread
                }

                val httpEditorTopForm = HttpEditorTopForm(file, module,fileEditor)

                httpEditorTopForm.initEnvCombo(it)

                fileEditor.putUserData(HttpEditorTopForm.KEY, httpEditorTopForm)

                source.addTopComponent(fileEditor, httpEditorTopForm.mainPanel)
            }
            .exceptionallyOnUiThread {
                NotifyUtil.notifyError(source.project, it.message)
            }
    }

}
