package org.javamaster.httpclient.action.dashboard

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.getUserData
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpBody
import org.javamaster.httpclient.psi.HttpOutputFile
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class CopyReqResBodyAction : DashboardBaseAction(nls("cy.body"), HttpIcons.COPY) {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = getHttpEditor(e)
        val project = editor.project!!
        val document = editor.document
        val httpFile = PsiDocumentManager.getInstance(project).getPsiFile(document)!!
        val copyPasteManager = CopyPasteManager.getInstance()

        val component = PlatformCoreDataKeys.CONTEXT_COMPONENT.getData(e.dataContext)!! as JComponent

        if (isReq(e)) {
            val text = PsiTreeUtil.findChildOfType(httpFile, HttpBody::class.java)?.text ?: return

            copyPasteManager.setContents(StringSelection(text))

            HintManager.getInstance().showSuccessHint(editor, "Copy request body success!")
        } else {
            val simpleTypeEnum = component.getUserData(httpDashboardResTypeKey) ?: return

            if (simpleTypeEnum.binary) {
                val outputFile = PsiTreeUtil.findChildOfType(httpFile, HttpOutputFile::class.java)!!

                copyPasteManager.setContents(StringSelection(outputFile.filePath!!.text))

                HintManager.getInstance().showSuccessHint(editor, "Copy file path success!")

                return
            }

            val text = PsiTreeUtil.findChildOfType(httpFile, HttpBody::class.java)?.text ?: return

            copyPasteManager.setContents(StringSelection(text))

            HintManager.getInstance().showSuccessHint(editor, "Copy response body success!")
        }

    }

}
