package org.javamaster.httpclient.action.dashboard

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Editor
import com.intellij.ui.popup.PopupFactoryImpl
import org.javamaster.httpclient.action.dashboard.view.*
import org.javamaster.httpclient.nls.NlsBundle.nls

/**
 * @author yudong
 */
class ViewSettingsAction(editor: Editor) : DashboardBaseAction(nls("view.settings"), AllIcons.General.InspectionsEye) {

    private val contentTypeActionGroup by lazy {
        ContentTypeActionGroup(editor)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = getHttpEditor(e)

        val actionGroup = DefaultActionGroup()

        val req = isReq(e)

        val showLineNumberAction = ShowLineNumberAction(editor, req)
        actionGroup.add(showLineNumberAction)

        val foldHeadersAction = FoldHeadersAction(editor, req)
        actionGroup.add(foldHeadersAction)

        actionGroup.addSeparator()

        actionGroup.addSeparator("View As")

        actionGroup.addAll(contentTypeActionGroup.actions)

        val jbPopupFactory = PopupFactoryImpl.getInstance()


        val listPopup = jbPopupFactory.createActionGroupPopup(
            null, actionGroup, e.dataContext,
            true, null, 16
        )

        listPopup.showUnderneathOf(e.inputEvent!!.component)
    }

}
