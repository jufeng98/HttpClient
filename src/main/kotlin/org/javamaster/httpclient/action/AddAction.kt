package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.env.EnvFileService.Companion.createEnvFile
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.NotifyUtil.notifyInfo
import org.javamaster.httpclient.utils.NotifyUtil.notifyWarn

/**
 * @author yudong
 */
abstract class AddAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {

        fun createAndReInitEnvCompo(isPrivate: Boolean, project: Project) {
            val envFileName = if (isPrivate) EnvFileService.PRIVATE_ENV_FILE_NAME else EnvFileService.ENV_FILE_NAME

            val envFile = createEnvFile(envFileName, isPrivate, project)
            if (envFile == null) {
                notifyWarn(project, envFileName + " " + nls("file.exists"))
                return
            }

            val fileEditorManager = FileEditorManager.getInstance(project)
            fileEditorManager.openFile(envFile, true)

            notifyInfo(project, nls("file.created") + " " + envFileName)

            try {
                val allEditors = fileEditorManager.allEditors
                for (editor in allEditors) {
                    val httpEditorTopForm = editor.getUserData(HttpEditorTopForm.KEY) ?: continue

                    val set = LinkedHashSet<String>()
                    set.add("dev")
                    set.add("uat")
                    set.add("pro")
                    httpEditorTopForm.initEnvCombo(set)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

}