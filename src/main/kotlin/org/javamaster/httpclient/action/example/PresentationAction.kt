package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

@Suppress("ActionPresentationInstantiatedInCtor")
class PresentationAction : ExampleAction(NlsBundle.nls("response.presentations")) {

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/responses-presentation.http")
    }

}
