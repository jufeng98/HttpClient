package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class AddPostMultipartTextAction : AddAction(NlsBundle.nls("post.multi.text.req")) {

    override fun actionPerformed(e: AnActionEvent) {
        startLiveTemplate("mptr")
    }

}