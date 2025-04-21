package org.javamaster.httpclient.action.addHttp

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import org.javamaster.httpclient.curl.CurlParser
import org.javamaster.httpclient.curl.support.CurlRequest
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * @author yudong
 */
class ImportCurlAction : AddAction() {
    override fun update(event: AnActionEvent) {
        event.presentation.text = NlsBundle.nls("import.curl")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = ProjectUtil.getActiveProject()!!

        val str = Messages.showInputDialog("", NlsBundle.nls("import.curl"), null) ?: return
        val curlRequest: CurlRequest
        try {
            curlRequest = CurlParser(str).parseToCurlRequest()
        } catch (e: Exception) {
            NotifyUtil.notifyError(project, e.message)
            return
        }

        val sb = StringBuilder()
        sb.append("\n")
        sb.append(curlRequest.httpMethod!!)
        sb.append(" ")
        sb.append(curlRequest.toString())
        sb.append("\n")
        curlRequest.headers.forEach {
            sb.append("${it.key}: ${it.value}")
        }
        sb.append("\n")

        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        val document = FileDocumentManager.getInstance().getDocument(editor.virtualFile)!!

        runWriteAction {
            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(document.textLength, sb.toString())

                editor.caretModel.moveToOffset(document.textLength)
            }
        }
    }

}