package org.javamaster.httpclient.utils

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ScrollingModel
import com.intellij.util.DocumentUtil
import org.apache.commons.lang3.time.DateFormatUtils
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

}