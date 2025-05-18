package org.javamaster.httpclient.action

import com.intellij.codeInsight.hint.HintManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.curl.CurlParser
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.psi.HttpRequestBlock
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.NotifyUtil
import java.awt.datatransfer.StringSelection

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ConvertToCurlAndCpAction : AnAction(nls("convert.to.curl.cp"), null, AllIcons.General.InlineCopy) {
    override fun update(e: AnActionEvent) {
        val requestBlock = findRequestBlock(e)

        e.presentation.isEnabled = requestBlock != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val requestBlock = findRequestBlock(e) ?: return

        val project = e.project!!

        convertToCurlAnCy(requestBlock, project, editor)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {

        fun findRequestBlock(e: AnActionEvent): HttpRequestBlock? {
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return null

            val httpFile = PsiUtil.getPsiFile(e.project!!, editor.virtualFile)

            val psiElement = httpFile.findElementAt(editor.caretModel.offset) ?: return null

            return PsiTreeUtil.getParentOfType(psiElement, HttpRequestBlock::class.java)
        }

        fun convertToCurlAnCy(requestBlock: HttpRequestBlock, project: Project, editor: Editor) {
            val method = requestBlock.request.method.text
            if (method == HttpRequestEnum.WEBSOCKET.name
                || method == HttpRequestEnum.DUBBO.name
            ) {
                NotifyUtil.notifyWarn(project, nls("convert.not.supported"))
                return
            }

            try {
                CurlParser.toCurlString(requestBlock, project, false) {
                    CopyPasteManager.getInstance().setContents(StringSelection(it))

                    val str = if (it.length > 2000) {
                        it.substring(0, 2000) + "$CR_LF......"
                    } else {
                        it
                    }

                    HintManager.getInstance().showInformationHint(editor, nls("converted.tip") + "\n" + str)
                }
            } catch (e: Exception) {
                NotifyUtil.notifyError(project, e.toString())
            }
        }

    }
}
