package org.javamaster.httpclient.runconfig

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.javamaster.httpclient.ui.ConfigSettingsForm
import javax.swing.JComponent

/**
 * @author yudong
 */
class HttpSettingsEditor(env: String, httpFilePath: String, project: Project) :
    SettingsEditor<HttpRunConfiguration>() {
    private val configSettingsForm = ConfigSettingsForm()

    init {
        configSettingsForm.initForm(env, httpFilePath, project)
    }

    override fun resetEditorFrom(runConfiguration: HttpRunConfiguration) {

    }

    override fun applyEditorTo(runConfiguration: HttpRunConfiguration) {
        val pair = configSettingsForm.pair

        runConfiguration.env = pair.first

        runConfiguration.httpFilePath = pair.second
    }

    override fun isReadyForApply(): Boolean {
        return true
    }

    override fun createEditor(): JComponent {
        return configSettingsForm.mainPanel
    }
}
