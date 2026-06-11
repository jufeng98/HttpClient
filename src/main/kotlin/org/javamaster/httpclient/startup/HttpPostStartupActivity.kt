package org.javamaster.httpclient.startup

import com.intellij.json.JsonFileType
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application
import com.intellij.util.concurrency.AppExecutorUtil
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.env.EnvFileService.Companion.getService
import org.javamaster.httpclient.jsPlugin.support.JavaScript
import org.javamaster.httpclient.logger.logInfo
import org.javamaster.httpclient.logger.logWarn
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.HttpUtils


/**
 * Add a top toolbar to the http file
 *
 * @author yudong
 */
class HttpPostStartupActivity : FileEditorManagerListener, ProjectActivity {
    @Volatile
    private var jsonAssociated = false

    override suspend fun execute(project: Project) {
        val fileEditorManager = FileEditorManager.getInstance(project)

        fileEditorManager.openFiles.forEach {
            handleFileOpened(fileEditorManager, it)
        }

        project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this)

        if (JavaScript.isAvailable()) {
            if (JavaScript.isTsLibraryNotInstalled(project)) {
                try {
                    JavaScript.installTsLibrary(project)
                } catch (t: Throwable) {
                    logWarn("安装ts库错误", t)
                }
            }

            if (JavaScript.isElementScopeNoRegister()) {
                try {
                    JavaScript.registerElementScopeProvider()
                } catch (t: Throwable) {
                    logWarn("注册element scope provider错误", t)
                }
            }
        }
    }

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        application.executeOnPooledThread {
            handleFileOpened(source, file)
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        val fileEditor = source.getSelectedEditor(file)
        fileEditor?.putUserData(HttpEditorTopForm.KEY, null)
    }

    private fun handleFileOpened(source: FileEditorManager, file: VirtualFile) {
        if (file.fileType !is HttpFileType) {
            return
        }

        val fileEditor = source.getSelectedEditor(file)
        if (fileEditor == null) {
            logWarn("Can't find file editor for ${file.path}")
            return
        }

        val project = source.project

        if (HttpUtils.isFileInHistoryDir(file, project)) {
            val textEditor = fileEditor as TextEditor
            textEditor.editor.document.setReadOnly(true)
            return
        }

        val module = ModuleUtil.findModuleForFile(file, project)

        if (jsonAssociated) {
            initTopForm(source, file, module, fileEditor, project)
            return
        }

        val fileTypeManagerEx = FileTypeManagerEx.getInstanceEx()

        val jsonFileType = JsonFileType.INSTANCE
        val jsonExtension = jsonFileType.defaultExtension

        val currentType = fileTypeManagerEx.getFileTypeByExtension(jsonExtension)
        if (currentType === jsonFileType) {
            jsonAssociated = true

            initTopForm(source, file, module, fileEditor, project)
            return
        }

        if (currentType != UnknownFileType.INSTANCE && currentType != PlainTextFileType.INSTANCE) {
            initTopForm(source, file, module, fileEditor, project)
            return
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
