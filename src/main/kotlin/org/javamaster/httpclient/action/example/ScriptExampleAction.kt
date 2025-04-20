package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

class ScriptExampleAction : ExampleAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.text = NlsBundle.nls("request.with.tests.and.scripts")
    }

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/requests-with-scripts.http")
    }

}
