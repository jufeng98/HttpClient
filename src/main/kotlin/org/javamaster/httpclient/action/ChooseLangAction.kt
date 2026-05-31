package org.javamaster.httpclient.action

import com.intellij.json.JsonLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.fileTypes.PlainTextLanguage
import org.javamaster.httpclient.ui.HttpDashboardForm
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * @author yudong
 */
class ChooseLangAction(private val httpDashboardForm: HttpDashboardForm) : ComboBoxAction() {
    private var langMap = linkedMapOf(
        "Text" to PlainTextLanguage.INSTANCE, "JSON" to JsonLanguage.INSTANCE,
        "XML" to XMLLanguage.INSTANCE, "HTML" to HTMLLanguage.INSTANCE
    )
    val actionGroup by lazy {
        val actions = langMap.keys.map { LangAction(it) }
        DefaultActionGroup(actions)
    }

    private var selectedLang = "JSON"

    private var comboBoxButton: ComboBoxButton? = null

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val button = createComboBoxButton(presentation)
        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        button.preferredSize = Dimension(48, button.preferredSize.height)

        val label = JLabel("Language:")
        label.preferredSize = Dimension(68, label.preferredSize.height)
        label.horizontalAlignment = SwingConstants.CENTER

        val panel = JPanel(BorderLayout())

        panel.add(label, BorderLayout.WEST)

        panel.add(button, BorderLayout.CENTER)

        return panel
    }

    override fun createComboBoxButton(presentation: Presentation): ComboBoxButton {
        presentation.description = "Switch language"
        presentation.text = selectedLang

        comboBoxButton = super.createComboBoxButton(presentation)
        comboBoxButton!!.border = null

        return comboBoxButton!!
    }

    override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
        return actionGroup
    }

    inner class LangAction(val lang: String) : AnAction(lang) {

        override fun actionPerformed(e: AnActionEvent) {
            comboBoxButton!!.presentation.text = lang
            selectedLang = lang

            httpDashboardForm.recreateWsReqEditor(langMap[selectedLang])
        }

    }

}