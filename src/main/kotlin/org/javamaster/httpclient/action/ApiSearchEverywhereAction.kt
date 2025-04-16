package org.javamaster.httpclient.action

import com.intellij.ide.actions.SearchEverywhereBaseAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.symbol.ApisGotoSEContributor

/**
 * @author yudong
 */
class ApiSearchEverywhereAction : SearchEverywhereBaseAction() {
    override fun update(event: AnActionEvent) {
        event.presentation.text = NlsBundle.nls("search.api")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val tabID = ApisGotoSEContributor::class.java.simpleName
        showInSearchEverywherePopup(tabID, e, true, false)
    }
}