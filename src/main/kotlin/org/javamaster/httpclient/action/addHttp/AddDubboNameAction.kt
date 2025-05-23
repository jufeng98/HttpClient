package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class AddDubboNameAction : AddAction(NlsBundle.nls("dubbo.req.name")) {

    override fun actionPerformed(e: AnActionEvent) {
        startLiveTemplate("dtrp")
    }

}