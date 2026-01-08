package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.utils.NotifyUtil
import java.util.Date

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ShowTimestampAction : AnAction(nls("show.timestamp"), null, null) {
    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val selectedText = editor?.selectionModel?.selectedText
        e.presentation.isEnabledAndVisible = selectedText != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val selectedText = editor?.selectionModel?.selectedText ?: return
        try {
            val timestamp = selectedText.toLong()
            val format = DateFormatUtils.format(Date(timestamp), "yyyy-MM-dd HH:mm:ss")
            NotifyUtil.notifyInfo(e.project!!, format)
        } catch (ex: Exception) {
            NotifyUtil.notifyWarn(e.project!!, "error: " + ex.message)
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
