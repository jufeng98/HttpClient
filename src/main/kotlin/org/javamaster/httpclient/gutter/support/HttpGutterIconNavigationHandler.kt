package org.javamaster.httpclient.gutter.support

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.psi.PsiElement
import org.javamaster.httpclient.dashboard.HttpProgramRunner
import org.javamaster.httpclient.dashboard.HttpProgramRunner.Companion.HTTP_RUNNER_ID
import org.javamaster.httpclient.psi.HttpMethod
import java.awt.event.MouseEvent

/**
 * @author yudong
 */
object HttpGutterIconNavigationHandler : GutterIconNavigationHandler<PsiElement> {

    override fun navigate(event: MouseEvent, element: PsiElement) {
        val gutterComponent = event.component as EditorGutterComponentEx?

        val httpProgramRunner = ProgramRunner.findRunnerById(HTTP_RUNNER_ID)!! as HttpProgramRunner

        httpProgramRunner.executeFromGutter(element.parent as HttpMethod, gutterComponent)
    }

}
