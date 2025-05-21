package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

@Suppress("ActionPresentationInstantiatedInCtor")
class AuthExampleAction : ExampleAction(NlsBundle.nls("request.with.authorization")) {

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/requests-with-authorization.http")
    }

}
