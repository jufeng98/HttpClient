package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

class DubboExampleAction : ExampleAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.text = NlsBundle.nls("dubbo.requests")
    }

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/dubbo-requests.http")
    }

}
