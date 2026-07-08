package org.javamaster.httpclient.listener

import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.psi.PsiDocumentManager
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpRequestBlock

/**
 * @author yudong
 */
class DisableDirectionEditorMouseListener(
    private val inlay: Inlay<out EditorCustomElementRenderer>,
    private val requestBlock: HttpRequestBlock,
    private val paramEnum: ParamEnum,
) : EditorMouseListener {

    override fun mouseClicked(e: EditorMouseEvent) {
        val point = e.mouseEvent.getPoint()
        val bounds = inlay.bounds
        if (bounds == null) {
            return
        }


        if (bounds.contains(point)) {
            val project = requestBlock.project

            val document = PsiDocumentManager.getInstance(project).getDocument(requestBlock.containingFile)

            if (document?.isWritable == true) {
                HttpFile.insertReqDirectionComment(requestBlock, paramEnum, project)
            }
        }
    }

}
