package org.javamaster.httpclient.runconfig

import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.components.JBTextField
import org.javamaster.httpclient.nls.NlsBundle
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author yudong
 */
class HttpSettingsEditor(private val env: String, private val httpFilePath: String) :
    SettingsEditor<HttpRunConfiguration>() {
    override fun resetEditorFrom(s: HttpRunConfiguration) {

    }

    override fun applyEditorTo(s: HttpRunConfiguration) {

    }

    override fun createEditor(): JComponent {
        val jPanel = JPanel(BorderLayout())

        jPanel.add(JBTextField("${NlsBundle.nls("env")} $env"), BorderLayout.NORTH)

        jPanel.add(JBTextField("${NlsBundle.nls("http.file")} $httpFilePath"), BorderLayout.CENTER)

        return jPanel
    }
}
