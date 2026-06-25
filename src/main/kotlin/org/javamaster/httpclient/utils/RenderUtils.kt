package org.javamaster.httpclient.utils

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.InlayProperties
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.listener.HttpEditorMouseListener
import org.javamaster.httpclient.listener.HttpEditorMouseMotionListener
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.renderer.FileEditorCustomElementRenderer
import org.javamaster.httpclient.renderer.TextEditorCustomElementRenderer
import java.util.*

/**
 * @author yudong
 */
object RenderUtils {
    private val inlayProperties: InlayProperties = InlayProperties()

    init {
        inlayProperties.showWhenFolded(true)
    }

    fun renderResDesc(resEditor: Editor, statusCode: Int, costTimes: Long?, contentLength: Int?) {
        val sec = costTimes!! / 1000.0
        val secDesc = "%.2f".format(Locale.getDefault(), sec)
        val sizeDesc = Formats.formatFileSize(contentLength?.toLong()!!)

        val text = nls(
            "res.desc", statusCode, ReflectionUtils.findStatusDesc(statusCode),
            costTimes, secDesc, contentLength, sizeDesc
        )

        val inlayModel = resEditor.inlayModel

        val inlayProperties = InlayProperties()
        inlayProperties.showWhenFolded(true)
        inlayProperties.relatesToPrecedingText(true)
        inlayProperties.showAbove(true)

        inlayModel.addBlockElement(0, inlayProperties, TextEditorCustomElementRenderer(resEditor, text))
    }

    fun renderResBodyFileName(resEditor: Editor, resDocument: Document, resBodyFile: VirtualFile, project: Project) {
        val inlayModel = resEditor.inlayModel

        val labelRenderer = TextEditorCustomElementRenderer(resEditor, nls("res.body.saved"))

        inlayModel.addBlockElement(resDocument.textLength, inlayProperties, labelRenderer)

        val fileRenderer = FileEditorCustomElementRenderer(resEditor, resBodyFile.name)

        val resBodyInlay = inlayModel.addBlockElement(resDocument.textLength, inlayProperties, fileRenderer)

        if (resBodyInlay == null) return

        val signWidth = fileRenderer.signWidth

        resEditor.addEditorMouseListener(HttpEditorMouseListener(resBodyInlay, resBodyFile, project, signWidth))

        resEditor.addEditorMouseMotionListener(HttpEditorMouseMotionListener(resBodyInlay, resEditor, signWidth))
    }

    fun renderCookieFilePath(
        resEditor: Editor,
        resDocument: Document,
        cookieSavePair: Pair<String, VirtualFile>,
        project: Project,
    ) {
        val inlayModel = resEditor.inlayModel

        inlayModel.addBlockElement(
            resDocument.textLength,
            inlayProperties,
            TextEditorCustomElementRenderer(resEditor, "")
        )

        val labelRenderer = TextEditorCustomElementRenderer(resEditor, cookieSavePair.first)

        inlayModel.addBlockElement(resDocument.textLength, inlayProperties, labelRenderer)

        val cookiesFile = cookieSavePair.second

        val fileRenderer = FileEditorCustomElementRenderer(resEditor, cookiesFile.path)

        val cookieInlay = inlayModel.addBlockElement(resDocument.textLength, inlayProperties, fileRenderer)

        if (cookieInlay == null) return

        val signWidth = fileRenderer.signWidth

        resEditor.addEditorMouseListener(HttpEditorMouseListener(cookieInlay, cookiesFile, project, signWidth))

        resEditor.addEditorMouseMotionListener(HttpEditorMouseMotionListener(cookieInlay, resEditor, signWidth))
    }

}