package org.javamaster.httpclient.action

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.env.EnvFileService.Companion.getService
import org.javamaster.httpclient.nls.NlsBundle.nls
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
class ChooseEnvironmentAction(private val file: VirtualFile) : ComboBoxAction() {
    private var comboBoxButton: ComboBoxButton? = null

    private var selectedEnv: String? = null

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val button = createComboBoxButton(presentation)
        button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        val jLabel = JLabel(nls("env"))
        jLabel.preferredSize = Dimension(38, jLabel.preferredSize.height)
        jLabel.horizontalAlignment = SwingConstants.CENTER

        val panel = JPanel(BorderLayout())

        panel.add(jLabel, BorderLayout.WEST)

        panel.add(button, BorderLayout.CENTER)

        return panel
    }

    override fun createComboBoxButton(presentation: Presentation): ComboBoxButton {
        presentation.description = nls("env.tooltip")
        presentation.text = selectedEnv ?: noEnv

        comboBoxButton = super.createComboBoxButton(presentation)
        comboBoxButton!!.border = null

        return comboBoxButton!!
    }

    override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
        val project = dataContext.getData(PlatformDataKeys.PROJECT)
            ?: return DefaultActionGroup.createPopupGroupWithEmptyText()

        val path = file.parent?.path ?: return DefaultActionGroup.createPopupGroupWithEmptyText()

        val envFileService = getService(project)
        val presetEnvSet = envFileService.getPresetEnvSet(path)

        val envList = mutableListOf(noEnv)
        envList.addAll(presetEnvSet)

        return DefaultActionGroup(envList.map { MyAction(it) })
    }

    fun getSelectedEnv(): String? {
        return selectedEnv
    }

    fun setSelectEnv(env: String) {
        selectedEnv = if (env.isEmpty() || env == noEnv) {
            null
        } else {
            env
        }

        comboBoxButton?.presentation?.text = selectedEnv ?: noEnv
    }

    private inner class MyAction(val env: String) : AnAction(env) {
        override fun actionPerformed(e: AnActionEvent) {
            setSelectEnv(env)

            DaemonCodeAnalyzer.getInstance(e.project!!).restart()
        }
    }

    companion object {
        val noEnv = nls("no.env")
    }

}
