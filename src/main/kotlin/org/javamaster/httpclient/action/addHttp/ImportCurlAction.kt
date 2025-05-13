package org.javamaster.httpclient.action.addHttp

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.Messages
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.javamaster.httpclient.curl.CurlParser
import org.javamaster.httpclient.curl.support.CurlRequest
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.NotifyUtil
import java.awt.datatransfer.DataFlavor

/**
 * @author yudong
 */
class ImportCurlAction : AddAction() {
    init {
        templatePresentation.text = NlsBundle.nls("import.from.curl")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = ProjectUtil.getActiveProject()!!

        val contents = CopyPasteManager.getInstance().getContents<String?>(DataFlavor.stringFlavor)

        var initialValue = "curl -i https://www.baidu.com"
        if (!contents.isNullOrEmpty()) {
            val trim = contents.trim()
            if (trim.startsWith("curl")) {
                initialValue = trim
            }
        }

        val curlStr = Messages.showMultilineInputDialog(
            project,
            null,
            NlsBundle.nls("import.from.curl"),
            initialValue,
            null,
            null
        ) ?: return

        val curlRequest: CurlRequest
        try {
            curlRequest = CurlParser(curlStr).parseToCurlRequest()
        } catch (e: Exception) {
            NotifyUtil.notifyError(project, e.message)
            return
        }

        val sb = StringBuilder()
        sb.append("\n\n")
        sb.append("### curl request\n")
        sb.append(curlRequest.httpMethod!!)
        sb.append(" ")
        sb.append(curlRequest.toString())
        sb.append("\n")
        curlRequest.headers.forEach {
            sb.append("${it.key}: ${it.value}\n")
        }
        sb.append("\n")

        val multipartBoundary = curlRequest.multipartBoundary
        if (multipartBoundary == null) {
            val textToSend = curlRequest.textToSend
            if (textToSend != null) {
                sb.append(textToSend)
                sb.append("\n\n")
            }
        } else {
            curlRequest.formBodyPart.forEach {
                sb.append("--${multipartBoundary}\n")

                val bodyPart = it.toBodyPart()
                for (field in bodyPart.header.fields) {
                    sb.append("${field.name}: ${field.body}\n")
                }

                sb.append("\n")

                val body = bodyPart.body
                if (body is FileBody) {
                    body.file
                    sb.append("< ${body.file.absolutePath.replace("\\", "/")}\n")
                } else if (body is StringBody) {
                    sb.append("${body.reader.readText()}\n")
                }
            }

            sb.append("--${multipartBoundary}--")
        }

        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        val document = FileDocumentManager.getInstance().getDocument(editor.virtualFile)!!

        runWriteAction {
            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(document.textLength, sb.toString())

                editor.caretModel.moveToOffset(document.textLength)

                editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
            }
        }
    }

}