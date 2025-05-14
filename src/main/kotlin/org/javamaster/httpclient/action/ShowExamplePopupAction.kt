package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import org.javamaster.httpclient.nls.NlsBundle.nls
import java.awt.Cursor
import javax.swing.JComponent

/**
 * @author yudong
 */
class ShowExamplePopupAction : ComboBoxAction() {
    init {
        templatePresentation.text = nls("http.examples")
        templatePresentation.description = nls("http.example.desc")
    }

    override fun createComboBoxButton(presentation: Presentation): ComboBoxButton {
        val button = super.createComboBoxButton(presentation)

        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        button.border = null

        return button
    }

    override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
        val actionManager = ActionManager.getInstance()
        return actionManager.getAction("exampleHttpGroup") as DefaultActionGroup
    }

}
