package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

class PostExampleAction : ExampleAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.text = NlsBundle.nls("post.requests")
    }

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/post-requests.http")
    }

}
