package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
class AddEnvPrivateFileAction : AddAction() {

    override fun update(event: AnActionEvent) {
        event.presentation.text = NlsBundle.nls("create.env.private.json.file")
    }

    override fun actionPerformed(e: AnActionEvent) {
        createAndReInitEnvCompo(true)
    }

}