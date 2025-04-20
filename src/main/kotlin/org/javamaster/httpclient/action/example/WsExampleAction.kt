package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

class WsExampleAction : ExampleAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.text = NlsBundle.nls("websocket.requests")
    }

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/ws-requests.http")
    }

}
