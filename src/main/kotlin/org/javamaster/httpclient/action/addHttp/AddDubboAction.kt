package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class AddDubboAction : AddAction(NlsBundle.nls("dubbo.req")) {

    override fun actionPerformed(e: AnActionEvent) {
        startLiveTemplate("dtr")
    }

}