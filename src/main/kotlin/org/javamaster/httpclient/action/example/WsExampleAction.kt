package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

@Suppress("ActionPresentationInstantiatedInCtor")
class WsExampleAction : ExampleAction(NlsBundle.nls("websocket.requests")) {

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/ws-requests.http")
    }

}
