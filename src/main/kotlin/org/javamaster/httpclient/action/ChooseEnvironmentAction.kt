package org.javamaster.httpclient.action

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.popup.PopupFactoryImpl
import org.javamaster.httpclient.env.EnvFileService.Companion.getService
import org.javamaster.httpclient.nls.NlsBundle.nls

/**
 * @author yudong
 */
class ChooseEnvironmentAction(private val file: VirtualFile) : AnAction() {
    private var selectedEnv: String? = null

    private val noEnv = nls("no.env")

    init {
        templatePresentation.text = noEnv
        templatePresentation.description = noEnv

        templatePresentation.icon = AllIcons.General.ChevronDown
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val path = file.parent?.path ?: return

        val envFileService = getService(project)
        val presetEnvSet = envFileService.getPresetEnvSet(path)

        val envList = mutableListOf(noEnv)
        envList.addAll(presetEnvSet)

        val jbPopupFactory = PopupFactoryImpl.getInstance()
        val popup = jbPopupFactory.createPopupChooserBuilder(envList)
            .setItemChosenCallback {
                setSelectEnv(it, e.presentation)

                DaemonCodeAnalyzer.getInstance(project).restart()
            }
            .createPopup()

        popup.showUnderneathOf(e.inputEvent!!.component)
    }

    fun getSelectedEnv(): String? {
        return selectedEnv
    }

    fun setSelectEnv(env: String, presentation: Presentation?) {
        selectedEnv = if (env == noEnv) {
            null
        } else {
            env
        }

        if (presentation != null) {
            presentation.text = env
            presentation.description = env
        } else {
            templatePresentation.text = env
            templatePresentation.description = env
        }
    }
}
