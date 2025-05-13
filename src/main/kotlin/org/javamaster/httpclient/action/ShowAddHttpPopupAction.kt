package org.javamaster.httpclient.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.ui.popup.PopupFactoryImpl
import org.javamaster.httpclient.nls.NlsBundle.nls

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ShowAddHttpPopupAction : AnAction(nls("add.to.http"), null, AllIcons.General.Add) {

    override fun actionPerformed(e: AnActionEvent) {
        val actionManager = ActionManager.getInstance()
        val popupFactory = PopupFactoryImpl.getInstance()

        val group = actionManager.getAction("addToHttpGroup") as ActionGroup

        val listPopup = popupFactory.createActionGroupPopup(
            nls("new"), group, DataContext.EMPTY_CONTEXT,
            true, null, 16
        )

        listPopup.showUnderneathOf(e.inputEvent!!.component)
    }

}
