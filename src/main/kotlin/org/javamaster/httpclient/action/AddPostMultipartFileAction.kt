package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle

/**
 * @author yudong
 */
class AddPostMultipartFileAction : AddAction() {
    override fun update(event: AnActionEvent) {
        event.presentation.text = NlsBundle.nls("post.multi.file.req")
    }

    override fun actionPerformed(e: AnActionEvent) {
    }

}