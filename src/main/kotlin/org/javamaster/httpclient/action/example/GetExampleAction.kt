package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle.nls

class GetExampleAction : ExampleAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.text = nls("get.requests")
    }

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/get-requests.http")
    }

}
