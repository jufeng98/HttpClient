package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
class AddEnvFileAction : AddAction() {

    override fun update(event: AnActionEvent) {
        event.presentation.text = NlsBundle.nls("create.env.json.file")
    }

    override fun actionPerformed(e: AnActionEvent) {
        createAndReInitEnvCompo(false)
    }

}