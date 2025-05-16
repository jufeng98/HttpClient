package org.javamaster.httpclient.action.dashboard.view

import com.intellij.icons.AllIcons
import com.intellij.json.JsonLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.injection.Injectable
import com.intellij.psi.util.PsiTreeUtil
import org.apache.http.entity.ContentType
import org.intellij.plugins.intelliLang.inject.InjectLanguageAction
import org.intellij.plugins.intelliLang.inject.UnInjectLanguageAction
import org.javamaster.httpclient.action.dashboard.DashboardBaseAction
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpMessageBody

/**
 * @author yudong
 */
class ContentTypeActionGroup(private val editor: Editor) {
    private val textAction = TextAction(setOf(ContentType.TEXT_PLAIN))
    private val jsonAction = JsonAction(setOf(ContentType.APPLICATION_JSON))
    private val xmlAction = XmlAction(setOf(ContentType.TEXT_XML, ContentType.APPLICATION_XML))
    private val htmlAction = HtmlAction(setOf(ContentType.TEXT_HTML))


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
        DashboardBaseAction(text, null) {
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
            val injectable = getInjectable()

            val project = editor.project!!
            val document = editor.document
            val httpFile = PsiDocumentManager.getInstance(project).getPsiFile(document) as HttpFile

            val messageBody = PsiTreeUtil.findChildOfType(httpFile, HttpMessageBody::class.java) ?: return

            val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
            val injectedPsiFile = injectedLanguageManager.getInjectedPsiFiles(messageBody)!![0].first as PsiFile

            val application = ApplicationManager.getApplication()

            application.runReadAction(Computable {
                UnInjectLanguageAction.invokeImpl(project, editor, injectedPsiFile)
                println("unInjectedPsiFile: ${injectedPsiFile.virtualFile.path}")
                ""
            })

            InjectLanguageAction.invokeImpl(project, editor, injectedPsiFile, injectable)
            println("injectedPsiFile: ${injectedPsiFile.virtualFile.path},injectable: ${injectable.toLanguage()}")

            switchActionContentType(allowContentTypes.iterator().next())
        }

        abstract fun getInjectable(): Injectable
    }


    private inner class TextAction(relateTypes: Set<ContentType>) : ContentTypeAction(relateTypes, "Text") {

        override fun getInjectable(): Injectable {
            return Injectable.fromLanguage(PlainTextLanguage.INSTANCE)!!
        }


    }

    private inner class JsonAction(relateTypes: Set<ContentType>) : ContentTypeAction(relateTypes, "JSON") {

        override fun getInjectable(): Injectable {
            return Injectable.fromLanguage(JsonLanguage.INSTANCE)!!
        }

    }

    private inner class XmlAction(relateTypes: Set<ContentType>) : ContentTypeAction(relateTypes, "XML") {

        override fun getInjectable(): Injectable {
            return Injectable.fromLanguage(XMLLanguage.INSTANCE)!!
        }

    }

    private inner class HtmlAction(relateTypes: Set<ContentType>) : ContentTypeAction(relateTypes, "HTML") {

        override fun getInjectable(): Injectable {
            return Injectable.fromLanguage(HTMLLanguage.INSTANCE)!!
        }

    }
}
