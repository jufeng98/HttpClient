package org.javamaster.httpclient.action.addHttp

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class AddPostParamAction : AddAction(NlsBundle.nls("post.param.req")) {

    override fun actionPerformed(e: AnActionEvent) {
        startLiveTemplate("ptrp")
    }

}