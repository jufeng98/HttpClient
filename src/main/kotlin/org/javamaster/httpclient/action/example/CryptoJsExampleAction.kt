package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

class CryptoJsExampleAction : ExampleAction() {

    override fun update(e: AnActionEvent) {
        e.presentation.text = NlsBundle.nls("show.cryptojs.file")
    }

    override fun actionPerformed(e: AnActionEvent) {
        openExample("examples/crypto-js.js")
    }

}
