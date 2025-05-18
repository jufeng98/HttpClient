package org.javamaster.httpclient.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.LightVirtualFile
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.action.ConvertToCurlAndCpAction.Companion.findRequestBlock
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ShowRequestHistoryAction : AnAction(nls("show.req.history"), null, AllIcons.General.Add) {
    override fun update(e: AnActionEvent) {
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

        val virtualFile = LightVirtualFile("$tabName-history", HttpFileType.INSTANCE, request.text)

        val editorManager = FileEditorManager.getInstance(project)
        editorManager.openFile(virtualFile)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
