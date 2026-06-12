package org.javamaster.httpclient.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.BottomLineBorder
import com.intellij.ui.components.JBLabel
import com.intellij.ui.popup.PopupFactoryImpl
import org.javamaster.httpclient.action.ChooseEnvironmentAction.Companion.noEnv
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.env.EnvFileService.Companion.getService
import org.javamaster.httpclient.nls.NlsBundle.nls
import java.util.function.Consumer

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class ChooseEnvBeforeRunService(private val project: Project) {
    private val popupFactory = PopupFactoryImpl.getInstance()

    fun createEnvChoosePopup(virtualFile: VirtualFile, consumer: Consumer<String>): JBPopup {
        val items = mutableListOf<Pair<String, JBLabel>>()

        val lastChooseEnv = virtualFile.getUserData(HttpConsts.lastPopupEnvChooseKey)
        if (lastChooseEnv != null) {
            val pair = convertToPair(lastChooseEnv)
            pair.second.border = BottomLineBorder()
            items.add(pair)
        }

        val envSet = getService(project).getPresetEnvSet(virtualFile.parent.path)
        for (env in envSet) {
            val pair = convertToPair(env)
            items.add(pair)
        }

        return popupFactory.createPopupChooserBuilder(items)
            .setRenderer { _, value, _, _, _ -> value.second }
            .setTitle(nls("choose.env"))
            .setItemChosenCallback {
                virtualFile.putUserData(HttpConsts.lastPopupEnvChooseKey, it.first)

                consumer.accept(it.first)
            }
            .createPopup()
    }

    fun convertToPair(env: String): Pair<String, JBLabel> {
        return if (env == noEnv) {
            val jbLabel = JBLabel(nls("run.with.no.env"))
            Pair(env, jbLabel)
        } else {
            val jbLabel = JBLabel(nls("run.with.env", env))
            Pair(env, jbLabel)
        }
    }

}
