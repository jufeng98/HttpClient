package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

@Suppress("ActionPresentationInstantiatedInCtor")
class ScriptExampleAction : ExampleAction(NlsBundle.nls("request.with.tests.and.scripts")) {

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/requests-with-scripts.http")
    }

}
