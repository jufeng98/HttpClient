package org.javamaster.httpclient.renderer

import com.intellij.openapi.editor.Editor
import com.intellij.ui.JBColor
import java.awt.Graphics

/**
 * @author yudong
 */
class ErrorTextEditorCustomElementRenderer(editor: Editor, text: String) :
    TextEditorCustomElementRenderer(editor, text) {

    override fun customizeGraphics(g: Graphics) {
        super.customizeGraphics(g)

        g.color = JBColor.RED
    }

}
