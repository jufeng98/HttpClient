package org.javamaster.httpclient.action.dashboard.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.FileContentUtilCore
import org.apache.http.entity.ContentType
import org.javamaster.httpclient.parser.HttpFile


/**
 * @author yudong
 */
class ContentTypeActionGroup(private val editor: Editor) {
    private val textAction = TextAction(setOf(ContentType.TEXT_PLAIN))
    private val jsonAction = JsonAction(setOf(ContentType.APPLICATION_JSON))
    private val xmlAction = XmlAction(setOf(ContentType.TEXT_XML, ContentType.APPLICATION_XML))
    private val htmlAction = HtmlAction(setOf(ContentType.TEXT_HTML, ContentType.APPLICATION_XHTML_XML))

    val actions = listOf(textAction, jsonAction, xmlAction, htmlAction)

    private val allowContentTypes = mutableSetOf<ContentType>()
    var contentType: ContentType?

    init {
        allowContentTypes.addAll(textAction.relateTypes)
        allowContentTypes.addAll(jsonAction.relateTypes)
        allowContentTypes.addAll(xmlAction.relateTypes)
        allowContentTypes.addAll(htmlAction.relateTypes)

        contentType = calContentType()

        if (contentType == null) {
            disableActions()
        } else {
            switchActionContentType(contentType!!)
        }
    }

    private fun disableActions() {
        actions.forEach {
            it.disableAction()
        }
    }

    private fun switchActionContentType(contentType: ContentType) {
        this.contentType = contentType

        actions.forEach {
            it.switchContentType(contentType)
        }
    }

    private fun calContentType(): ContentType? {
        val project = editor.project!!
        val document = editor.document
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
        if (psiFile !is HttpFile) {
            return null
        }

        val contentType = psiFile.getRequestBlocks()[0].request.contentType ?: return null

        if (!allowContentTypes.contains(contentType)) {
            return null
        }

        return contentType
    }

    abstract inner class ContentTypeAction(val relateTypes: Set<ContentType>, text: String) :
        AnAction(text, null, null) {

        fun disableAction() {
            templatePresentation.isEnabled = false
        }

        fun switchContentType(contentType: ContentType) {
            if (relateTypes.contains(contentType)) {
                templatePresentation.icon = AllIcons.Actions.Checked
            } else {
                templatePresentation.icon = null
            }
        }

        override fun actionPerformed(e: AnActionEvent) {
            val project = editor.project!!
            val document = editor.document
            val httpFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
            if (httpFile !is HttpFile) {
                return
            }

            if (contentType == null) {
                return
            }

            val type = relateTypes.iterator().next()

            httpFile.virtualFile.putUserData(httpDashboardContentTypeKey, type)

            switchActionContentType(type)

            FileContentUtilCore.reparseFiles(httpFile.virtualFile)
        }

    }

    private inner class TextAction(relateTypes: Set<ContentType>) : ContentTypeAction(relateTypes, "Text")

    private inner class JsonAction(relateTypes: Set<ContentType>) : ContentTypeAction(relateTypes, "JSON")

    private inner class XmlAction(relateTypes: Set<ContentType>) : ContentTypeAction(relateTypes, "XML")

    private inner class HtmlAction(relateTypes: Set<ContentType>) : ContentTypeAction(relateTypes, "HTML")

    companion object {
        val httpDashboardContentTypeKey = Key.create<ContentType>("org.javamaster.dashboard.httpDashboardContentType")
    }
}
