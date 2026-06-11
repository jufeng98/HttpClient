package org.javamaster.httpclient.action

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.nls.NlsBundle.nls
import java.util.*

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class VisualizeTimestampAction : AnAction(nls("visualize.timestamp"), nls("visualize.timestamp.desc"), null) {

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val selectedText = editor?.selectionModel?.selectedText
        e.presentation.isEnabledAndVisible = selectedText != null && selectedText.length >= 10
                && StringUtils.isNumeric(selectedText)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val selectedText = editor?.selectionModel?.selectedText ?: return

        val timestamp = if (selectedText.length >= 13) selectedText.toLong() else selectedText.toLong() * 1000

        val format = DateFormatUtils.format(Date(timestamp), HttpConsts.JAVA_DATE_PATTERN)

        HintManager.getInstance().showInformationHint(editor, format)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
