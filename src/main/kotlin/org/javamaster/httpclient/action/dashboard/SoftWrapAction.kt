package org.javamaster.httpclient.action.dashboard

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.getUserData
import com.intellij.ui.JBColor
import org.javamaster.httpclient.nls.NlsBundle.nls
import java.awt.Dimension
import javax.swing.JComponent

/**
 * @author yudong
 */
class SoftWrapAction(private val editor: Editor) :
    DashboardBaseAction(nls("soft.wrap"), AllIcons.Actions.ToggleSoftWrap), CustomComponentAction {

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val actionButton = ActionButton(this, presentation, place, Dimension(20, 20))

        val req = editor.component.getUserData(httpDashboardToolbarKey)!!
        if (req) {
            setSoftWrap(reqUseSoftWrap, actionButton)
        } else {
            setSoftWrap(resUseSoftWrap, actionButton)
        }

        return actionButton
    }

    override fun actionPerformed(e: AnActionEvent) {
        val actionButton = e.inputEvent!!.component as ActionButton

        val useSoftWrap = if (isReq(e)) {
            reqUseSoftWrap = !reqUseSoftWrap
            reqUseSoftWrap
        } else {
            resUseSoftWrap = !resUseSoftWrap
            resUseSoftWrap
        }

        setSoftWrap(useSoftWrap, actionButton)

    }

    private fun setSoftWrap(useSoftWrap: Boolean, actionButton: ActionButton) {
        val settings = editor.settings
        settings.isUseSoftWraps = useSoftWrap
        if (useSoftWrap) {
            actionButton.background = JBColor.LIGHT_GRAY
        } else {
            actionButton.background = null
        }
    }

    companion object {
        private var reqUseSoftWrap: Boolean = false
        private var resUseSoftWrap: Boolean = false
    }
}
