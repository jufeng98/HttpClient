package org.javamaster.httpclient.action.ws

import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.messageBus.WsLangChangeNotifier
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
class ChooseWsLangAction(private val project: Project, parentDisposable: Disposable) : ComboBoxAction() {
    private var langMap = linkedMapOf(
        "Text" to PlainTextLanguage.INSTANCE, "JSON" to JsonLanguage.INSTANCE,
        "XML" to XMLLanguage.INSTANCE, "HTML" to HTMLLanguage.INSTANCE
    )
    private val actionGroup by lazy {
        val actions = langMap.keys.map { LangAction(it) }
        DefaultActionGroup(actions)
    }
    private var selectedLang = "JSON"
    private var comboBoxButton: ComboBoxButton? = null

    init {
        project.messageBus.connect(parentDisposable).subscribe(
            WsLangChangeNotifier.WS_LANG_CHANGE_TOPIC,
            object : WsLangChangeNotifier {
                override fun change(newLanguage: Language) {
                    switchLang(newLanguage)
                }
            })
    }

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val button = createComboBoxButton(presentation)
        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        button.preferredSize = Dimension(58, button.preferredSize.height)

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

    fun switchLang(language: Language) {
        langMap.entries.forEach {
            val key = it.key
            val value = it.value

            if (value == language) {
                comboBoxButton!!.presentation.text = key
                selectedLang = key
            }
        }
    }

    inner class LangAction(val lang: String) : AnAction(lang) {

        override fun actionPerformed(e: AnActionEvent) {
            project.messageBus.syncPublisher(WsLangChangeNotifier.WS_LANG_CHANGE_TOPIC).change(langMap[lang]!!)
        }

    }

}