package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class AddEnvFileAction : AddAction(NlsBundle.nls("create.env.json.file")) {

    override fun actionPerformed(e: AnActionEvent) {
        createAndReInitEnvCompo(false)
    }

}