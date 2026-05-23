package org.javamaster.httpclient.action.addHttp

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import org.javamaster.httpclient.utils.HttpUtils


/**
 * @author yudong
 */
abstract class AddAction(name: String) : AnAction(name) {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    fun startLiveTemplate(abbreviation: String) {
        val project = HttpUtils.getActiveValidProject() ?: return

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

}