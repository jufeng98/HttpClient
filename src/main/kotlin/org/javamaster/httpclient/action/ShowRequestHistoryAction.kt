package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.util.application
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.action.ConvertToCurlAndCpAction.Companion.findRequestBlock
import org.javamaster.httpclient.curl.CurlParser
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.CR_LF
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.VirtualFileUtils
import java.io.File

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ShowRequestHistoryAction : AnAction(nls("show.req.history"), null, HttpIcons.History) {
    override fun update(e: AnActionEvent) {
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)

        if (HttpUtils.isHistoryFile(virtualFile)) {
            e.presentation.isEnabled = false
            return
        }

        val requestBlock = findRequestBlock(e)

        e.presentation.isEnabled = requestBlock != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val requestBlock = findRequestBlock(e) ?: return

        val request = requestBlock.request
        val project = e.project!!

        val method = request.method.text
        if (method == HttpRequestEnum.WEBSOCKET.name
            || method == HttpRequestEnum.DUBBO.name
        ) {
            NotifyUtil.notifyWarn(project, nls("convert.not.supported"))
            return
        }

        val tabName = HttpUtils.getTabName(request.method)

        val dateHistoryDir = VirtualFileUtils.getDateHistoryDir(project)
        val bodyFilesFolder = File(dateHistoryDir, tabName)

        val listFiles = bodyFilesFolder.listFiles()
        if (listFiles == null) {
            NotifyUtil.notifyWarn(project, nls("no.res.body.files"))
            return
        }

        try {
            CurlParser.toCurlString(requestBlock, project, true) {
                application.executeOnPooledThread {
                    val historyBodyFileStrList = listFiles
                        .map { historyBodyFile ->
                            "<> ${tabName}/${historyBodyFile.name}"
                        }
                        .take(6)
                        .joinToString(CR_LF)

                    val content = it + CR_LF + historyBodyFileStrList

                    runInEdt {
                        WriteAction.run<Exception> {
                            val virtualFile = VirtualFileUtils.createHistoryHttpVirtualFile(content, project, tabName)

                            val editorManager = FileEditorManager.getInstance(project)
                            editorManager.openFile(virtualFile)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            NotifyUtil.notifyError(project, e.toString())
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
