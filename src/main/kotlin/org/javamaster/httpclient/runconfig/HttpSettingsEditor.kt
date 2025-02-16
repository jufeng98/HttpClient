package org.javamaster.httpclient.runconfig

import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.components.JBTextField
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
        val jPanel = JPanel()
        jPanel.layout = BorderLayout()
        jPanel.add(JBTextField("环境:$env"), BorderLayout.NORTH)
        jPanel.add(JBTextField("http文件:$httpFilePath"), BorderLayout.CENTER)
        return jPanel
    }
}
