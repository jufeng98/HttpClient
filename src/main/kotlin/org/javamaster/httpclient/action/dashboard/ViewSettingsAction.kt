package org.javamaster.httpclient.action.dashboard

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Editor
import com.intellij.ui.popup.PopupFactoryImpl
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.action.dashboard.view.ContentTypeActionGroup
import org.javamaster.httpclient.action.dashboard.view.FoldHeadersAction
import org.javamaster.httpclient.action.dashboard.view.ShowLineNumberAction
import org.javamaster.httpclient.nls.NlsBundle.nls


/**
 * @author yudong
 */
class ViewSettingsAction(editor: Editor) : DashboardBaseAction(nls("view.settings"), HttpIcons.INSPECTIONS_EYE) {

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

        actionGroup.addSeparator("View As")

        actionGroup.addAll(contentTypeActionGroup.actions)

        val jbPopupFactory = PopupFactoryImpl.getInstance()
        val actionManager = ActionManager.getInstance()

        val actionToolbar = actionManager.createActionToolbar("httpViewSettingsToolBar", actionGroup, false)
        actionToolbar.setShowSeparatorTitles(true)

        jbPopupFactory.createComponentPopupBuilder(actionToolbar.component, null)
            .createPopup()
            .showUnderneathOf(e.inputEvent!!.component)
    }

}
