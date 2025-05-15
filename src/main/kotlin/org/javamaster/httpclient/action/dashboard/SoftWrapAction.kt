package org.javamaster.httpclient.action.dashboard

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.ui.JBColor
import org.javamaster.httpclient.nls.NlsBundle.nls
import java.awt.Color

/**
 * @author yudong
 */
class SoftWrapAction : DashboardBaseAction(nls("soft.wrap"), AllIcons.Actions.ToggleSoftWrap) {
    private var useSoftWrap = false
    private var originalColor: Color? = null

    override fun actionPerformed(e: AnActionEvent) {
        val editor = getHttpEditor(e)
        val settings = editor.settings
        val actionButton = e.inputEvent!!.component as ActionButton

        if (originalColor == null) {
            originalColor = actionButton.background
        }

        useSoftWrap = !useSoftWrap

        if (useSoftWrap) {
            settings.isUseSoftWraps = true
            actionButton.background = JBColor.LIGHT_GRAY
        } else {
            settings.isUseSoftWraps = false
            actionButton.background = originalColor
        }
    }

}
