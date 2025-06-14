package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.curl.CurlParser
import org.javamaster.httpclient.curl.support.CurlRequest
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.CurlUtils
import org.javamaster.httpclient.utils.NotifyUtil
import java.awt.datatransfer.DataFlavor

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ImportCurlAction : AddAction(NlsBundle.nls("import.from.curl")) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project!!

        val contents = CopyPasteManager.getInstance().getContents<String?>(DataFlavor.stringFlavor)

        var initialValue = "curl -i https://www.baidu.com"
        if (!contents.isNullOrEmpty()) {
            if (CurlUtils.isCurlString(contents)) {
                initialValue = contents
            }
        }

        var curlRequestTmp: CurlRequest? = null

        val curlStr = Messages.showMultilineInputDialog(
            project, null, NlsBundle.nls("import.from.curl"), initialValue, HttpIcons.FILE,
            object : InputValidator {
                override fun checkInput(str: String?): Boolean {
                    return true
                }

                override fun canClose(str: String?): Boolean {
                    str ?: return false

                    try {
                        curlRequestTmp = CurlParser(str).parseToCurlRequest()
                    } catch (e: Exception) {
                        NotifyUtil.notifyError(project, e.toString())

                        return false
                    }

                    return true
                }
            }
        ) ?: return

        val curlRequest = curlRequestTmp!!

        val httpStr = toHttpRequest(curlRequest, curlStr)


        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        val document = FileDocumentManager.getInstance().getDocument(editor.virtualFile)!!

        runWriteAction {
            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(document.textLength, httpStr)

                editor.caretModel.moveToOffset(document.textLength)

                editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
            }
        }
    }

    companion object {

        fun toHttpRequest(curlRequest: CurlRequest, curlStr: String): String {
            val sb = StringBuilder()
            sb.append("\n\n")
            sb.append("### curl request\n")
            sb.append("/*\n")
            sb.append(CurlUtils.createCurlStringComment(curlStr))
            sb.append("*/\n")
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
                        sb.append("< ${body.file.absolutePath.replace("\\", "/")}\n")
                    } else if (body is StringBody) {
                        sb.append("${body.reader.readText()}\n")
                    }
                }

                sb.append("--${multipartBoundary}--")
            }

            return sb.toString()
        }

    }

}