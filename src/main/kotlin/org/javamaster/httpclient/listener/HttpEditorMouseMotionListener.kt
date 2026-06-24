package org.javamaster.httpclient.listener

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import org.javamaster.httpclient.renderer.HttpEditorCustomElementRenderer
import java.awt.Cursor
import java.awt.Rectangle

/**
 * @author yudong
 */
class HttpEditorMouseMotionListener(
    private val resBodyInlay: Inlay<HttpEditorCustomElementRenderer>,
    private val resEditor: Editor,
    private val signWidth: Int,
) : EditorMouseMotionListener {

    override fun mouseMoved(e: EditorMouseEvent) {
        val point = e.mouseEvent.getPoint()
        val bounds = resBodyInlay.bounds
        if (bounds == null) {
            return
        }

        val boundsCp = Rectangle(bounds.x + signWidth, bounds.y, bounds.width - signWidth, bounds.height)

        val contains = boundsCp.contains(point)

        val cursor = if (contains) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else Cursor.getDefaultCursor()

        resEditor.contentComponent.setCursor(cursor)
    }

}
