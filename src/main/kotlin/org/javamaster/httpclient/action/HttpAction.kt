package org.javamaster.httpclient.action

import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import org.javamaster.httpclient.dashboard.HttpProgramRunner
import org.javamaster.httpclient.dashboard.HttpProgramRunner.Companion.HTTP_RUNNER_ID
import org.javamaster.httpclient.psi.HttpMethod

/**
 * Handle click event and sending request
 *
 * @author yudong
 */
class HttpAction(private val httpMethod: HttpMethod) : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val gutterComponent = e.inputEvent?.component as EditorGutterComponentEx?

        val httpProgramRunner = ProgramRunner.findRunnerById(HTTP_RUNNER_ID)!! as HttpProgramRunner

        httpProgramRunner.executeFromGutter(httpMethod, gutterComponent)
    }

}
