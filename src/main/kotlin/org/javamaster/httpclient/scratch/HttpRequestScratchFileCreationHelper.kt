package org.javamaster.httpclient.scratch

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.scratch.ScratchFileCreationHelper
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import org.javamaster.httpclient.HttpLanguage

/**
 * @author yudong
 */
class HttpRequestScratchFileCreationHelper : ScratchFileCreationHelper() {
    private val templateName = "HTTP Scratch"

    override fun prepareText(project: Project, context: Context, dataContext: DataContext): Boolean {
        if (context.language !== HttpLanguage.INSTANCE || StringUtil.isNotEmpty(context.text)) return false

        val file = createFileFromTemplate(project)

        context.text = file + "\n"
        context.caretOffset = context.text.length

        return true
    }

    private fun createFileFromTemplate(project: Project): String {
        val template = FileTemplateManager.getInstance(project).findInternalTemplate(templateName)

        return template.getText(FileTemplateManager.getInstance(project).defaultProperties)
    }
}
