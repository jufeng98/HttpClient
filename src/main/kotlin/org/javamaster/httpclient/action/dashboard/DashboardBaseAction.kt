package org.javamaster.httpclient.action.dashboard

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.getUserData
import org.javamaster.httpclient.consts.HttpConsts
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author yudong
 */
abstract class DashboardBaseAction(text: String, icon: Icon?) : AnAction(text, null, icon) {

    fun getHttpEditor(e: AnActionEvent): Editor {
        val component = PlatformCoreDataKeys.CONTEXT_COMPONENT.getData(e.dataContext)!! as JComponent

        val req = component.getUserData(HttpConsts.httpDashboardToolbarKey)!!

        return if (req) {
            component.getUserData(HttpConsts.httpDashboardReqEditorKey)!!
        } else {
            component.getUserData(HttpConsts.httpDashboardResEditorKey)!!
        }
    }

    fun isReq(e: AnActionEvent): Boolean {
        val component = PlatformCoreDataKeys.CONTEXT_COMPONENT.getData(e.dataContext)!! as JComponent

        return component.getUserData(HttpConsts.httpDashboardToolbarKey)!!
    }

}
