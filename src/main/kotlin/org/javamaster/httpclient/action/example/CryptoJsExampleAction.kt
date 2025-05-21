package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

@Suppress("ActionPresentationInstantiatedInCtor")
class CryptoJsExampleAction : ExampleAction(NlsBundle.nls("show.cryptojs.file")) {

    override fun actionPerformed(e: AnActionEvent) {
        openExample("js/crypto-js.js")
    }

}
