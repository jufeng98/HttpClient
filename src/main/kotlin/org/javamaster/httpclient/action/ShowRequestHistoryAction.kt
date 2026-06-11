package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.util.application
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.action.ConvertToCurlAndCpAction.Companion.findRequestBlock
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.PathUtils
import org.javamaster.httpclient.utils.VirtualFileUtils
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ShowRequestHistoryAction : AnAction(nls("show.req.history"), null, HttpIcons.HISTORY) {

    override fun update(e: AnActionEvent) {
        val requestBlock = findRequestBlock(e)

        e.presentation.isEnabled = requestBlock != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val requestBlock = findRequestBlock(e) ?: return

        val request = requestBlock.request ?: return

        val project = e.project ?: return

        val method = request.method

        val tabName = HttpUtils.getTabName(method)
        val legalTabName = PathUtils.legalizeFileName(tabName)

        val dateHistoryDir = VirtualFileUtils.getDateHistoryDir(project)
        val bodyFilesFolder = File(dateHistoryDir, legalTabName)

        val listFiles = bodyFilesFolder.listFiles()
        if (listFiles == null) {
            NotifyUtil.notifyWarn(project, nls("no.res.body.files"))
            return
        }

        val url = request.requestTarget?.text ?: return

        application.executeOnPooledThread {
            val historyResFileList = listFiles
                .map { "<> ${legalTabName}/${it.name}" }
                .take(30)
                .joinToString(CR_LF)

            var content = "### $tabName$CR_LF"
            content += "${method.text} $url$CR_LF"

            content += CR_LF + historyResFileList

            val virtualFile = VirtualFileUtils.createHistoryHttpVirtualFile(project, legalTabName)

            runInEdt {
                WriteAction.run<Exception> {
                    virtualFile.setBinaryContent(content.toByteArray(StandardCharsets.UTF_8))

                    FileEditorManager.getInstance(project).openFile(virtualFile)
                }
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
