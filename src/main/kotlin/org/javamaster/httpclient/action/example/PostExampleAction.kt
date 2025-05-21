package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

@Suppress("ActionPresentationInstantiatedInCtor")
class PostExampleAction : ExampleAction(NlsBundle.nls("post.requests")) {

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/post-requests.http")
    }

}
