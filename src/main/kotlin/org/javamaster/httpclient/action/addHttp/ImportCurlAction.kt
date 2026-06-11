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
        val project = e.project ?: return

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

        val httpStr = CurlUtils.toHttpRequest(curlRequest, curlStr)

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

}