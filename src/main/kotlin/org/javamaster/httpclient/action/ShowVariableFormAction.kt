package org.javamaster.httpclient.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.ui.ViewVariableForm

/**
 * @author yudong
 */
@Suppress("ActionPresentationInstantiatedInCtor")
class ShowVariableFormAction : AnAction(nls("show.variables"), null, AllIcons.General.InlineVariables) {

    override fun actionPerformed(e: AnActionEvent) {
        val viewVariableForm = ViewVariableForm(e.project)
        viewVariableForm.show()
    }

}
