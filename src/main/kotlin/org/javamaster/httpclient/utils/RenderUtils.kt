package org.javamaster.httpclient.utils

import com.intellij.codeInsight.daemon.impl.HintRenderer
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.InlayProperties
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.Formats
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.enums.ParamEnum
import org.javamaster.httpclient.js.support.JsExecuteResult
import org.javamaster.httpclient.listener.ChangeCursorEditorMouseMotionListener
import org.javamaster.httpclient.listener.DisableDirectionEditorMouseListener
import org.javamaster.httpclient.listener.OpenFileEditorMouseListener
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.renderer.ErrorTextEditorCustomElementRenderer
import org.javamaster.httpclient.renderer.FileEditorCustomElementRenderer
import org.javamaster.httpclient.renderer.TextEditorCustomElementRenderer
import java.util.*

/**
 * @author yudong
 */
object RenderUtils {
    private val inlayProperties: InlayProperties = InlayProperties()
    private val inlayPropertiesSec = InlayProperties()

    init {
        inlayProperties.showWhenFolded(true)

        inlayPropertiesSec.showWhenFolded(true)
        inlayPropertiesSec.relatesToPrecedingText(true)
        inlayPropertiesSec.showAbove(true)
    }

    fun createReqDescRender(
        resEditor: Editor,
        reqContentLength: Long,
    ): TextEditorCustomElementRenderer {
        val sizeDesc = Formats.formatFileSize(reqContentLength)

        val text = nls("req.size", reqContentLength, sizeDesc)

        return TextEditorCustomElementRenderer(resEditor, text)
    }

    fun createResDescRender(
        resEditor: Editor,
        statusCode: Int,
        costTimes: Long?,
        contentLength: Int?,
    ): TextEditorCustomElementRenderer {
        val sec = costTimes!! / 1000.0
        val secDesc = "%.2f".format(Locale.getDefault(), sec)
        val sizeDesc = Formats.formatFileSize(contentLength?.toLong()!!)

        val text = nls(
            "res.desc", statusCode, ReflectionUtils.findStatusDesc(statusCode),
            costTimes, secDesc, contentLength, sizeDesc
        )

        return TextEditorCustomElementRenderer(resEditor, text)
    }

    fun createJsExecuteResultRender(
        editor: Editor,
        jsExecuteResult: JsExecuteResult,
    ): MutableList<TextEditorCustomElementRenderer> {
        val mutableList = jsExecuteResult.resList
            .map {
                TextEditorCustomElementRenderer(editor, it)
            }
            .toMutableList()

        val exception = jsExecuteResult.jsScriptException
        if (exception != null) {
            val cause = exception.cause!!
            mutableList.add(ErrorTextEditorCustomElementRenderer(editor, "$cause"))
        }

        return mutableList
    }

    fun renderTop(resEditor: Editor, list: List<TextEditorCustomElementRenderer>) {
        val inlayModel = resEditor.inlayModel

        for (renderer in list.reversed()) {
            inlayModel.addBlockElement(0, inlayPropertiesSec, renderer)
        }
    }

    fun renderResBodyFileName(
        resEditor: Editor,
        resDocument: Document,
        resBodyFile: VirtualFile,
        project: Project,
        requestBlock: HttpRequestBlock,
    ) {
        val inlayModel = resEditor.inlayModel

        val labelRenderer = TextEditorCustomElementRenderer(resEditor, nls("res.body.saved"))

        inlayModel.addBlockElement(resDocument.textLength, inlayProperties, labelRenderer)

        val disableInlay = inlayModel.addBlockElement(resDocument.textLength, inlayProperties, HintRenderer("Disable"))

        if (disableInlay != null) {
            val listener = DisableDirectionEditorMouseListener(disableInlay, requestBlock, ParamEnum.NO_LOG)

            resEditor.addEditorMouseListener(listener)

            resEditor.addEditorMouseMotionListener(ChangeCursorEditorMouseMotionListener(disableInlay, resEditor))
        }

        val fileRenderer = FileEditorCustomElementRenderer(resEditor, resBodyFile.name)

        val resBodyInlay = inlayModel.addBlockElement(resDocument.textLength, inlayProperties, fileRenderer)

        if (resBodyInlay == null) return

        val signWidth = fileRenderer.signWidth

        resEditor.addEditorMouseListener(OpenFileEditorMouseListener(resBodyInlay, resBodyFile, project, signWidth))

        resEditor.addEditorMouseMotionListener(
            ChangeCursorEditorMouseMotionListener(resBodyInlay, resEditor, signWidth)
        )
    }

    fun renderCookieFilePath(
        resEditor: Editor,
        resDocument: Document,
        cookieSavePair: Pair<String, VirtualFile>,
        project: Project,
        requestBlock: HttpRequestBlock,
    ) {
        val inlayModel = resEditor.inlayModel

        val emptyRenderer = TextEditorCustomElementRenderer(resEditor, "")

        inlayModel.addBlockElement(resDocument.textLength, inlayProperties, emptyRenderer)

        val labelRenderer = TextEditorCustomElementRenderer(resEditor, cookieSavePair.first)

        inlayModel.addBlockElement(resDocument.textLength, inlayProperties, labelRenderer)
        val disableInlay = inlayModel.addBlockElement(
            resDocument.textLength, inlayProperties, HintRenderer("Disable")
        )

        if (disableInlay != null) {
            val listener = DisableDirectionEditorMouseListener(disableInlay, requestBlock, ParamEnum.NO_COOKIE_JAR)

            resEditor.addEditorMouseListener(listener)

            resEditor.addEditorMouseMotionListener(ChangeCursorEditorMouseMotionListener(disableInlay, resEditor))
        }

        val cookiesFile = cookieSavePair.second

        val fileRenderer = FileEditorCustomElementRenderer(resEditor, cookiesFile.path)

        val cookieInlay = inlayModel.addBlockElement(resDocument.textLength, inlayProperties, fileRenderer)

        if (cookieInlay == null) return

        val signWidth = fileRenderer.signWidth

        resEditor.addEditorMouseListener(OpenFileEditorMouseListener(cookieInlay, cookiesFile, project, signWidth))

        resEditor.addEditorMouseMotionListener(ChangeCursorEditorMouseMotionListener(cookieInlay, resEditor, signWidth))
    }

}