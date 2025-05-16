package org.javamaster.httpclient.action.dashboard.view

import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.getUserData
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager
import org.javamaster.httpclient.action.dashboard.DashboardBaseAction
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.HttpFile

/**
 * @author yudong
 */
class FoldHeadersAction(private val editor: Editor, private val req: Boolean) :
    DashboardBaseAction(nls("fold.headers.default"), null) {
    init {
        val foldHeader = if (req) {
            reqFoldHeader
        } else {
            resFoldHeader
        }

        if (foldHeader) {
            templatePresentation.icon = AllIcons.Actions.Checked
        } else {
            templatePresentation.icon = null
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val foldHeader = if (req) {
            reqFoldHeader = !reqFoldHeader
            reqFoldHeader
        } else {
            resFoldHeader = !resFoldHeader
            resFoldHeader
        }

        setFoldHeader(foldHeader)
    }

    private fun setFoldHeader(foldHeader: Boolean) {
        val component = editor.component
        component.getUserData(httpDashboardResTypeKey) ?: return

        setEditorFoldHeader(foldHeader, editor)

        if (foldHeader) {
            templatePresentation.icon = AllIcons.Actions.Checked
        } else {
            templatePresentation.icon = null
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

            val header = psiFile.getRequestBlocks()[0].request.header ?: return

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
