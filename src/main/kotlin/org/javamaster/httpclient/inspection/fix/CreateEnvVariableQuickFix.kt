package org.javamaster.httpclient.inspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.env.EnvFileService
import org.javamaster.httpclient.env.EnvFileService.Companion.ENV_FILE_NAME
import org.javamaster.httpclient.env.EnvFileService.Companion.PRIVATE_ENV_FILE_NAME
import org.javamaster.httpclient.env.EnvFileService.Companion.getEnvJsonFile
import org.javamaster.httpclient.ui.HttpEditorTopForm

/**
 * @author yudong
 */
class CreateEnvVariableQuickFix(private val isPrivate: Boolean, private val variableName: String) : LocalQuickFix {

    override fun getFamilyName(): String {
        val tip = if (isPrivate) "private" else ""
        return "Create variable unsolved in environment $tip file"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        createJsonProperty(project, variableName)
    }

    private fun createJsonProperty(project: Project, variableName: String) {
        if (!ApplicationManager.getApplication().isDispatchThread) {
            return
        }

        val topForm = HttpEditorTopForm.getSelectedEditorTopForm(project) ?: return

        val envFileName = if (isPrivate) PRIVATE_ENV_FILE_NAME else ENV_FILE_NAME

        val httpFileParentPath = topForm.file.parent.path

        val jsonFile = getEnvJsonFile(envFileName, httpFileParentPath, project)
        if (jsonFile == null) {
            topForm.createAndReInitEnvCompo(isPrivate)
            topForm.setSelectEnv("dev")
        } else {
            topForm.selectedEnv ?: return
            val fileEditorManager = FileEditorManager.getInstance(project)
            fileEditorManager.openFile(jsonFile.virtualFile, true)
        }

        val selectEnv = topForm.selectedEnv!!
        val envFileService = EnvFileService.getService(project)

        envFileService.createEnvValue(variableName, selectEnv, httpFileParentPath, envFileName)
    }
}
