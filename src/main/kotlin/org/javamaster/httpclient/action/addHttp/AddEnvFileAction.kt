package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.EnvFileUtils

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class AddEnvFileAction : AddAction(NlsBundle.nls("create.env.json.file")) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        EnvFileUtils.createAndReInitEnvCompo(false, project)
    }

}