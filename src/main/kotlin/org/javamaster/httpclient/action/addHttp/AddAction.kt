package org.javamaster.httpclient.action.addHttp

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
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

    fun startLiveTemplate(abbreviation: String) {
        val project = ProjectUtil.getActiveProject()!!
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        val document = editor.document

        if (!document.isWritable) {
            return
        }

        runWriteAction {
            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(document.textLength, "\n")

                editor.caretModel.moveToOffset(document.textLength)

                val templateManager = TemplateManager.getInstance(project)
                val template = TemplateSettings.getInstance().getTemplate(abbreviation, "HTTP Request")!!
                templateManager.startTemplate(editor, template)
            }
        }

    }

    companion object {

        fun createAndReInitEnvCompo(isPrivate: Boolean) {
            val project = ProjectUtil.getActiveProject()!!

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