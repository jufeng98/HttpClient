package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class AddEnvPrivateFileAction : AddAction(NlsBundle.nls("create.env.private.json.file")) {

    override fun actionPerformed(e: AnActionEvent) {
        createAndReInitEnvCompo(true)
    }

}