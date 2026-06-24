package org.javamaster.httpclient.listener

import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.renderer.HttpEditorCustomElementRenderer
import java.awt.Rectangle

/**
 * @author yudong
 */
class HttpEditorMouseListener(
    private val resBodyInlay: Inlay<HttpEditorCustomElementRenderer>,
    private val resBodyFile: VirtualFile,
    private val project: Project,
    private val signWidth: Int,
) : EditorMouseListener {

    override fun mouseClicked(e: EditorMouseEvent) {
        val point = e.mouseEvent.getPoint()
        val bounds = resBodyInlay.bounds
        if (bounds == null) {
            return
        }

        val boundsCp = Rectangle(bounds.x + signWidth, bounds.y, bounds.width - signWidth, bounds.height)

        if (boundsCp.contains(point)) {
            FileEditorManager.getInstance(project).openFile(resBodyFile)
        }
    }

}
