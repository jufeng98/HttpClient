package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class MockServerExampleAction : ExampleAction("Mock Server") {

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/mock-server.http")
    }

}
