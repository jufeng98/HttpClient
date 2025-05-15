package org.javamaster.httpclient.action.dashboard

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.getUserData
import com.intellij.openapi.util.Key
import org.javamaster.httpclient.enums.SimpleTypeEnum
import javax.swing.Icon
import javax.swing.JComponent

/**
 * @author yudong
 */
abstract class DashboardBaseAction(text: String, icon: Icon?) : AnAction(text, null, icon) {

    fun getHttpEditor(e: AnActionEvent): Editor {
        val component = PlatformCoreDataKeys.CONTEXT_COMPONENT.getData(e.dataContext)!! as JComponent

        val req = component.getUserData(httpDashboardToolbarKey)!!

        return if (req) {
            component.getUserData(httpDashboardReqEditorKey)!!
        } else {
            component.getUserData(httpDashboardResEditorKey)!!
        }
    }

    companion object {
        val httpDashboardToolbarKey = Key.create<Boolean>("org.javamaster.dashboard.httpDashboardToolbar")
        val httpDashboardResTypeKey = Key.create<SimpleTypeEnum?>("org.javamaster.dashboard.httpDashboardResType")

        val httpDashboardReqEditorKey = Key.create<Editor>("org.javamaster.dashboard.httpDashboardReqEditor")
        val httpDashboardResEditorKey = Key.create<Editor>("org.javamaster.dashboard.httpDashboardResEditor")
    }
}
