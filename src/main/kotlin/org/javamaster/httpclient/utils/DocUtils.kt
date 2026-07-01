package org.javamaster.httpclient.utils

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.editor.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.DocumentUtil
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import org.javamaster.httpclient.utils.VirtualFileUtils.createDescListVirtualFile
import java.util.*

/**
 * @author yudong
 */
object DocUtils {

    fun appendLog(editor: Editor, log: String) {
        runInEdt {
            val caret: Caret = editor.caretModel.primaryCaret
            val scrollingModel: ScrollingModel = editor.scrollingModel
            val document: Document = editor.document

            DocumentUtil.writeInRunUndoTransparentAction {
                val time = DateFormatUtils.format(Date(), "yyyy-MM-dd HH:mm:ss,SSS")
                val replace = log.replace(HttpUtils.CR_LF, "\n")
                val txt = "$time - $replace"

                document.insertString(document.textLength, txt)
                caret.moveToOffset(document.textLength)
                scrollingModel.scrollToCaret(ScrollType.RELATIVE)
            }
        }
    }

    fun createMockDoc(tabName: String, project: Project): Pair<VirtualFile, Document> {
        val fileDocumentManager = FileDocumentManager.getInstance()

        val virtualFile = createDescListVirtualFile(mutableListOf(""), "mock-server.log", tabName, false, project)

        val document = computeReadAction { fileDocumentManager.getDocument(virtualFile)!! }

        return Pair(virtualFile, document)
    }

}