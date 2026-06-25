package org.javamaster.httpclient.renderer

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import java.awt.Graphics
import java.awt.Rectangle

/**
 * @author yudong
 */
open class TextEditorCustomElementRenderer(editor: Editor, val text: String) :
    EditorCustomElementRenderer {
    private val editorFont = editor.contentComponent.font
    private val fontMetrics = editor.contentComponent.getFontMetrics(editorFont)

    private val width = fontMetrics.stringWidth(text)
    private val height = fontMetrics.height + 6

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        return width
    }

    override fun calcHeightInPixels(inlay: Inlay<*>): Int {
        return height
    }

    open fun customizeGraphics(g: Graphics) {
        g.color = JBColor.BLACK

        g.font = editorFont
    }

    override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
        val x = targetRegion.x
        val y = targetRegion.y + targetRegion.height - 6

        customizeGraphics(g)

        g.drawString(text, x, y)
    }

}
