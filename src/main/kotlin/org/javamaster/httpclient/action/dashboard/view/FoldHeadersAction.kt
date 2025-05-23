package org.javamaster.httpclient.action.dashboard.view

import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.getUserData
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.action.dashboard.DashboardBaseAction
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.HttpFile
import java.awt.Dimension
import javax.swing.JComponent

/**
 * @author yudong
 */
class FoldHeadersAction(private val editor: Editor, private val req: Boolean) :
    DashboardBaseAction(nls("fold.headers.default"), null), CustomComponentAction {

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val actionButtonWithText = ActionButtonWithText(this, presentation, place, Dimension(20, 20))

        val foldHeader = getFoldHeader()

        setFoldHeader(foldHeader, actionButtonWithText)

        return actionButtonWithText
    }

    override fun actionPerformed(e: AnActionEvent) {
        val actionButtonWithText = e.inputEvent!!.component as ActionButtonWithText

        val foldHeader = switchFoldHeader()

        setFoldHeader(foldHeader, actionButtonWithText)
    }

    private fun setFoldHeader(foldHeader: Boolean, actionButtonWithText: ActionButtonWithText) {
        val component = editor.component
        component.getUserData(httpDashboardResTypeKey) ?: return

        setEditorFoldHeader(foldHeader, editor)

        if (foldHeader) {
            actionButtonWithText.presentation.icon = AllIcons.Actions.Checked
        } else {
            actionButtonWithText.presentation.icon = HttpIcons.BLANK
        }
    }

    private fun getFoldHeader(): Boolean {
        return if (req) {
            reqFoldHeader
        } else {
            resFoldHeader
        }
    }

    private fun switchFoldHeader(): Boolean {
        return if (req) {
            reqFoldHeader = !reqFoldHeader
            reqFoldHeader
        } else {
            resFoldHeader = !resFoldHeader
            resFoldHeader
        }
    }

    companion object {
        val httpDashboardFoldHeaderKey = Key.create<Boolean>("org.javamaster.dashboard.httpDashboardFoldHeader")
        var reqFoldHeader: Boolean = true
        var resFoldHeader: Boolean = true

        fun setEditorFoldHeader(foldHeader: Boolean, editor: Editor) {
            val project = editor.project!!
            val document = editor.document
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
            if (psiFile !is HttpFile) {
                return
            }

            val requestBlocks = psiFile.getRequestBlocks()
            if (requestBlocks.isEmpty()) {
                return
            }

            val header = requestBlocks[0].request.header ?: return

            val foldingModel = editor.foldingModel

            val foldRegions = FoldingUtil.getFoldRegionsAtOffset(editor, header.textRange.startOffset)
            if (foldRegions.isEmpty()) {
                return
            }

            foldingModel.runBatchFoldingOperation {
                foldRegions[0].isExpanded = !foldHeader
            }
        }
    }

}
