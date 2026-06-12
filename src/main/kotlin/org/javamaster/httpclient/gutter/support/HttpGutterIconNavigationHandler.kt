package org.javamaster.httpclient.gutter.support

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.psi.PsiElement
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.application
import org.javamaster.httpclient.action.ChooseEnvironmentAction.Companion.isChooseEnvBeforeRun
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.dashboard.HttpProgramRunner
import org.javamaster.httpclient.dashboard.HttpProgramRunner.Companion.HTTP_RUNNER_ID
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.service.ChooseEnvBeforeRunService
import org.javamaster.httpclient.ui.HttpEditorTopForm
import java.awt.event.MouseEvent

/**
 * @author yudong
 */
object HttpGutterIconNavigationHandler : GutterIconNavigationHandler<PsiElement> {
    private val httpProgramRunner = ProgramRunner.findRunnerById(HTTP_RUNNER_ID)!! as HttpProgramRunner

    override fun navigate(event: MouseEvent, element: PsiElement) {
        val method = element.parent as HttpMethod
        val project = method.project

        val editorTopForm = HttpEditorTopForm.getSelectedEditorTopForm(project)
        if (isChooseEnvBeforeRun(editorTopForm?.selectedEnv)) {
            val virtualFile = method.containingFile.virtualFile
            val relativePoint = RelativePoint(event)

            project.getService(ChooseEnvBeforeRunService::class.java)
                .createEnvChoosePopup(virtualFile) {
                    executeRequest(event, method, it)
                }
                .show(relativePoint)
        } else {
            executeRequest(event, method, null)
        }
    }

    private fun executeRequest(event: MouseEvent, method: HttpMethod, targetEnv: String?) {
        val gutterComponent = event.component as EditorGutterComponentEx?
        val loadingRemover = gutterComponent?.setLoadingIconForCurrentGutterMark()

        application.executeOnPooledThread {
            method.putUserData(HttpConsts.runFileRequestIdxKey, null)

            httpProgramRunner.executeFromGutter(method, loadingRemover, targetEnv)
        }
    }

}
