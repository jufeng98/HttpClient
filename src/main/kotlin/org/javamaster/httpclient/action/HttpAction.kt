package org.javamaster.httpclient.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import org.javamaster.httpclient.gutter.HttpGutterIconClickHandler
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.ui.HttpEditorTopForm

/**
 * 处理点击事件并发起请求
 *
 * @author yudong
 */
class HttpAction(private val httpMethod: HttpMethod) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val component = e.inputEvent.component as EditorGutterComponentEx
        val navigationHandler = HttpGutterIconClickHandler(httpMethod)

        val selectedEnv = HttpEditorTopForm.getCurrentEditorSelectedEnv(httpMethod.project)
        navigationHandler.doRequest(component, selectedEnv)
    }

}
