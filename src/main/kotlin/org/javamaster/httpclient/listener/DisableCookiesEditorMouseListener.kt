package org.javamaster.httpclient.listener

import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpRequestBlock

/**
 * @author yudong
 */
class DisableCookiesEditorMouseListener(
    private val inlay: Inlay<out EditorCustomElementRenderer>,
    private val requestBlock: HttpRequestBlock,
) : EditorMouseListener {

    override fun mouseClicked(e: EditorMouseEvent) {
        val point = e.mouseEvent.getPoint()
        val bounds = inlay.bounds
        if (bounds == null) {
            return
        }


        if (bounds.contains(point)) {
            val project = requestBlock.project

            HttpFile.insertReqDirectionComment(requestBlock, ParamEnum.NO_COOKIE_JAR, project)
        }
    }

}
