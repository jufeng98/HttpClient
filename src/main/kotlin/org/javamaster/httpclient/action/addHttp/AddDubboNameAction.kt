package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
class AddDubboNameAction : AddAction() {
    override fun update(event: AnActionEvent) {
        event.presentation.text = NlsBundle.nls("dubbo.req.name")
    }

    override fun actionPerformed(e: AnActionEvent) {
        startLiveTemplate("dtrp")
    }

}