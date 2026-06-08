package org.javamaster.httpclient.action.dashboard

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.ui.getUserData
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpBody
import org.javamaster.httpclient.psi.HttpOutputFile
import org.javamaster.httpclient.utils.NotifyUtil
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class CopyReqResBodyAction : DashboardBaseAction(nls("cy.body"), HttpIcons.COPY) {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = getHttpEditor(e)
        val project = editor.project ?: return

        val document = editor.document
        val httpFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return

        val copyPasteManager = CopyPasteManager.getInstance()

        val component = PlatformCoreDataKeys.CONTEXT_COMPONENT.getData(e.dataContext) as? JComponent? ?: return

        if (isReq(e)) {
            val text = PsiTreeUtil.findChildOfType(httpFile, HttpBody::class.java)?.text ?: return

            copyPasteManager.setContents(StringSelection(text))

            NotifyUtil.notifyInfo(project, "Copy request body success!")
        } else {
            val simpleTypeEnum = component.getUserData(HttpConsts.httpDashboardResTypeKey) ?: return

            if (simpleTypeEnum.binary) {
                val outputFile = PsiTreeUtil.findChildOfType(httpFile, HttpOutputFile::class.java) ?: return

                copyPasteManager.setContents(StringSelection(outputFile.filePath?.text ?: return))

                NotifyUtil.notifyInfo(project, "Copy file path success!")

                return
            }

            val text = PsiTreeUtil.findChildOfType(httpFile, HttpBody::class.java)?.text ?: return

            copyPasteManager.setContents(StringSelection(text))

            NotifyUtil.notifyInfo(project, "Copy response body success!")
        }

    }

}
