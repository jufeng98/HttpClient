package org.javamaster.httpclient.action.dashboard.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.editor.Editor
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.action.dashboard.DashboardBaseAction
import org.javamaster.httpclient.nls.NlsBundle.nls
import java.awt.Dimension
import javax.swing.JComponent

/**
 * @author yudong
 */
class ShowLineNumberAction(private val editor: Editor, private val req: Boolean) :
    DashboardBaseAction(nls("show.line.num"), null), CustomComponentAction {

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val actionButtonWithText = ActionButtonWithText(this, presentation, place, Dimension(20, 20))

        val showLineNum = getShowLineNum()

        setShowLineNum(showLineNum, actionButtonWithText)

        return actionButtonWithText
    }

    override fun actionPerformed(e: AnActionEvent) {
        val actionButtonWithText = e.inputEvent!!.component as ActionButtonWithText

        val showLineNum = switchShowLineNum()

        setShowLineNum(showLineNum, actionButtonWithText)
    }

    private fun setShowLineNum(showLineNum: Boolean, actionButtonWithText: ActionButtonWithText) {
        editor.settings.isLineNumbersShown = showLineNum

        if (showLineNum) {
            actionButtonWithText.presentation.icon = AllIcons.Actions.Checked
        } else {
            actionButtonWithText.presentation.icon = HttpIcons.BLANK
        }
    }

    private fun getShowLineNum(): Boolean {
        return if (req) {
            reqShowLineNum
        } else {
            resShowLineNum
        }
    }

    private fun switchShowLineNum(): Boolean {
        return if (req) {
            reqShowLineNum = !reqShowLineNum
            reqShowLineNum
        } else {
            resShowLineNum = !resShowLineNum
            resShowLineNum
        }
    }

    companion object {
        var reqShowLineNum: Boolean = true
        var resShowLineNum: Boolean = true
    }

}
