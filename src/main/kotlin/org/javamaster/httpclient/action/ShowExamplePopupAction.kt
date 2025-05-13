package org.javamaster.httpclient.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.ui.popup.PopupFactoryImpl
import org.javamaster.httpclient.nls.NlsBundle.nls

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ShowExamplePopupAction : AnAction(nls("http.example.desc"), nls("http.examples"), AllIcons.General.ChevronDown) {

    override fun actionPerformed(e: AnActionEvent) {
        val actionManager = ActionManager.getInstance()
        val popupFactory = PopupFactoryImpl.getInstance()

        val group = actionManager.getAction("exampleHttpGroup") as ActionGroup

        val listPopup = popupFactory.createActionGroupPopup(
            nls("http.examples"), group, DataContext.EMPTY_CONTEXT,
            true, null, 10
        )

        listPopup.showUnderneathOf(e.inputEvent!!.component)
    }

}
