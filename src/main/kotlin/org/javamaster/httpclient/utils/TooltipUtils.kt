package org.javamaster.httpclient.utils

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

/**
 * @author yudong
 */
object TooltipUtils {

    fun showTooltip(msg: String, project: Project) {
        runInEdt {
            val textEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@runInEdt
            HintManager.getInstance().showInformationHint(textEditor, msg)
        }
    }

}
