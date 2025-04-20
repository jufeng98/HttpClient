package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

class PresentationAction : ExampleAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.text = NlsBundle.nls("response.presentations")
    }

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/responses-presentation.http")
    }

}
