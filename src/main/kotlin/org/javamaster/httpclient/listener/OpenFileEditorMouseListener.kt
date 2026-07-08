package org.javamaster.httpclient.listener

import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiManager
import org.javamaster.httpclient.renderer.FileEditorCustomElementRenderer
import java.awt.Rectangle
import java.io.File

/**
 * @author yudong
 */
class OpenFileEditorMouseListener(
    private val resBodyInlay: Inlay<FileEditorCustomElementRenderer>,
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

        if (boundsCp.contains(point) && resBodyFile.isValid) {
            val editors = FileEditorManager.getInstance(project).openFile(resBodyFile)

            if (editors.isNotEmpty()) return

            val psiFile = PsiManager.getInstance(project).findFile(resBodyFile)
            if (psiFile is PsiBinaryFile) {
                RevealFileAction.openFile(resBodyFile.toNioPath())
                return
            }

            val pathname = project.basePath
            if (pathname != null && VfsUtil.isAncestor(File(pathname), resBodyFile.toNioPath().toFile(), true)) {
                if (psiFile != null) {
                    psiFile.navigate(true)
                } else {
                    val psiDirectory = PsiManager.getInstance(project).findDirectory(resBodyFile.parent)
                    psiDirectory?.navigate(true)
                }
            } else {
                RevealFileAction.openFile(resBodyFile.toNioPath())
            }
        }
    }

}
