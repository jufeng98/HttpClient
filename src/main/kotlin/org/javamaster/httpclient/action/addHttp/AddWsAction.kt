package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
class AddWsAction : AddAction() {
    override fun update(event: AnActionEvent) {
        event.presentation.text = NlsBundle.nls("ws.req")
    }

    override fun actionPerformed(e: AnActionEvent) {
        startLiveTemplate("wsr")
    }

}