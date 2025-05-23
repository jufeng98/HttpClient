package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class AddPostJsonAction : AddAction(NlsBundle.nls("post.json.req")) {

    override fun actionPerformed(e: AnActionEvent) {
        startLiveTemplate("ptr")
    }

}