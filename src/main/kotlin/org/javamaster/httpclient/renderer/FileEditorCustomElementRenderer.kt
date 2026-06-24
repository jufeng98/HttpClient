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
class FileEditorCustomElementRenderer(editor: Editor, private val name: String) :
    EditorCustomElementRenderer {
    private val sign = ">> "
    private val text = sign + name

    private val editorFont = editor.contentComponent.font
    private val fontMetrics = editor.contentComponent.getFontMetrics(editorFont)

    private val width = fontMetrics.stringWidth(text)
    private val height = fontMetrics.height

    val signWidth = fontMetrics.stringWidth(sign)

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        return width
    }

    override fun calcHeightInPixels(inlay: Inlay<*>): Int {
        return height
    }

    override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
        val x = targetRegion.x
        val y = targetRegion.y + targetRegion.height

        g.drawString(sign, x, y)

        g.color = JBColor.BLUE

        g.drawString(name, x + signWidth, y)
        g.drawLine(x + signWidth, y + 2, x + targetRegion.width, y + 2)
    }

}
