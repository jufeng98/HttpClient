package org.javamaster.httpclient.action.dashboard

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.ScrollType
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.nls.NlsBundle.nls

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ScrollToEndAction : DashboardBaseAction(nls("scroll.to.end"), HttpIcons.SCROLL_DOWN) {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = getHttpEditor(e)

        val caret = editor.caretModel
        caret.moveToOffset(editor.document.textLength)

        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
    }

}
