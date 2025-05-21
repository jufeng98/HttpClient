package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle.nls

@Suppress("ActionPresentationInstantiatedInCtor")
class GetExampleAction : ExampleAction(nls("get.requests")) {

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/get-requests.http")
    }

}
