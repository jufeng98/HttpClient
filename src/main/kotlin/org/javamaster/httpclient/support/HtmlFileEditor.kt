package org.javamaster.httpclient.support

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiUtil
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.jcef.JCEFHtmlPanel
import org.javamaster.httpclient.enums.SimpleTypeEnum
import org.javamaster.httpclient.ui.HtmlPreviewForm
import java.beans.PropertyChangeListener
import javax.swing.JComponent

/**
 * @author yudong
 */
class HtmlFileEditor(project: Project, private val resBodyFile: VirtualFile) : FileEditor,
    UserDataHolderBase() {
    private val htmlPreviewForm = HtmlPreviewForm()

    private val jcefHtmlPanel = JCEFHtmlPanel(null)
    private val textEditor: Editor

    init {
        val pdf = resBodyFile.extension.equals(SimpleTypeEnum.PDF.type, ignoreCase = true)
        val html = if (pdf) {
            """
                <html>
                <body style="margin: 0">
                    <div>
                      <object data="file:///${resBodyFile.path}" type="application/pdf" width="100%" height="100%"></object>
                    </div>
                </body>
                </html>
            """.trimIndent()
        } else {
            resBodyFile.readText()
        }

        jcefHtmlPanel.setHtml(html)

        val psiDocumentManager = PsiDocumentManager.getInstance(project)

        val psiFile = PsiUtil.getPsiFile(project, LightVirtualFile("res.html", HtmlFileType.INSTANCE, html))
        val document = psiDocumentManager.getDocument(psiFile)!!

        val editorFactory = EditorFactory.getInstance()
        textEditor = editorFactory.createEditor(document, project, HtmlFileType.INSTANCE, true)

        htmlPreviewForm.initTabs(jcefHtmlPanel.component, textEditor.component, pdf)
    }

    override fun getFile(): VirtualFile {
        return resBodyFile
    }

    override fun dispose() {
        jcefHtmlPanel.dispose()
        EditorFactory.getInstance().releaseEditor(textEditor)
    }

    override fun getComponent(): JComponent {
        htmlPreviewForm.tabbedPane.selectedIndex = 0

        return htmlPreviewForm.mainPanel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        htmlPreviewForm.tabbedPane.selectedIndex = 0

        return htmlPreviewForm.mainPanel
    }

    override fun getName(): String {
        return "HtmlFileEditor"
    }

    override fun setState(fileEditorState: FileEditorState) {

    }

    override fun isModified(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
    }
}
