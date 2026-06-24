package org.javamaster.httpclient.listener

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import org.javamaster.httpclient.renderer.FileEditorCustomElementRenderer
import java.awt.Cursor
import java.awt.Rectangle

/**
 * @author yudong
 */
class HttpEditorMouseMotionListener(
    private val resBodyInlay: Inlay<FileEditorCustomElementRenderer>,
    private val resEditor: Editor,
    private val signWidth: Int,
) : EditorMouseMotionListener {

    private var lastContains = false

    override fun mouseMoved(e: EditorMouseEvent) {
        val bounds = resBodyInlay.bounds

        val boundsCp = if (bounds == null) {
            null
        } else {
            Rectangle(bounds.x + signWidth, bounds.y, bounds.width - signWidth, bounds.height)
        }

        val contains = boundsCp?.contains(e.mouseEvent.point) == true

        // 仅在状态变化时更新光标，避免持续设置
        if (contains != lastContains) {
            lastContains = contains

            // 2. 移出时用 TEXT_CURSOR（I型）替代 DEFAULT_CURSOR（箭头）
            val cursor = if (contains) {
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            } else {
                Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR)
            }

            resEditor.contentComponent.cursor = cursor
        }
    }

}