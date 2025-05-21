package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

@Suppress("ActionPresentationInstantiatedInCtor")
class DubboExampleAction : ExampleAction(NlsBundle.nls("dubbo.requests")) {

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/dubbo-requests.http")
    }

}
