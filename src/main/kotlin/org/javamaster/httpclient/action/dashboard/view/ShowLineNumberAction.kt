package org.javamaster.httpclient.action.dashboard.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import org.javamaster.httpclient.action.dashboard.DashboardBaseAction
import org.javamaster.httpclient.nls.NlsBundle.nls

/**
 * @author yudong
 */
class ShowLineNumberAction(private val editor: Editor, private val req: Boolean) :
    DashboardBaseAction(nls("show.line.num"), null) {
    init {
        val showLineNum = if (req) {
            reqShowLineNum
        } else {
            resShowLineNum
        }

        if (showLineNum) {
            templatePresentation.icon = AllIcons.Actions.Checked
        } else {
            templatePresentation.icon = null
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val showLineNum = if (req) {
            reqShowLineNum = !reqShowLineNum
            reqShowLineNum
        } else {
            resShowLineNum = !resShowLineNum
            resShowLineNum
        }

        setShowLineNum(showLineNum)
    }

    private fun setShowLineNum(showLineNum: Boolean) {
        editor.settings.isLineNumbersShown = showLineNum

        if (showLineNum) {
            templatePresentation.icon = AllIcons.Actions.Checked
        } else {
            templatePresentation.icon = null
        }
    }

    companion object {
        var reqShowLineNum: Boolean = true
        var resShowLineNum: Boolean = true
    }

}
