package org.javamaster.httpclient.gutter.support

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.application
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.dashboard.HttpProgramRunner
import org.javamaster.httpclient.dashboard.HttpProgramRunner.Companion.HTTP_RUNNER_ID
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpRunCommand
import org.javamaster.httpclient.service.RunHttpFileService
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.MyPsiUtils
import org.javamaster.httpclient.utils.NotifyUtil
import java.awt.event.MouseEvent
import java.io.File

/**
 * @author yudong
 */
object HttpRunGutterIconNavigationHandler : GutterIconNavigationHandler<PsiElement> {

    override fun navigate(event: MouseEvent, element: PsiElement) {
        val gutterComponent = event.component as EditorGutterComponentEx?
        val loadingRemover = gutterComponent?.setLoadingIconForCurrentGutterMark()

        val runCommand = element.parent as HttpRunCommand
        val path = runCommand.filePath?.text ?: return

        val httpFile = runCommand.containingFile as HttpFile
        val parentPath = httpFile.virtualFile.parent.path
        val project = httpFile.project

        application.executeOnPooledThread {
            if (HttpUtils.isRunTabName(path)) {
                runTargetFileRequest(path, httpFile, project, parentPath, loadingRemover)
            } else {
                runTargetFileRequests(path, project, parentPath, loadingRemover)
            }
        }
    }

    private fun runTargetFileRequest(
        name: String,
        httpFile: HttpFile,
        project: Project,
        parentPath: String,
        loadingRemover: Runnable?,
    ) {
        val targetTabName = HttpUtils.getTargetTabName(name)
        if (targetTabName == null) {
            NotifyUtil.notifyWarn(project, nls("req.not.exists", name))

            runInEdt { loadingRemover?.run() }

            return
        }

        val pairs = MyPsiUtils.getImportFileHttpRequests(httpFile, project, parentPath)
        val method = pairs
            .firstOrNull {
                var comment = it.first.text
                val tabName = comment.substring(3).trim()
                tabName == targetTabName
            }
            ?.second

        if (method == null) {
            NotifyUtil.notifyWarn(project, nls("req.not.exists", targetTabName))

            runInEdt { loadingRemover?.run() }

            return
        }

        val httpProgramRunner = ProgramRunner.findRunnerById(HTTP_RUNNER_ID)!! as HttpProgramRunner

        method.putUserData(HttpConsts.runFileRequestIdxKey, null)

        httpProgramRunner.executeFromGutter(method, loadingRemover)
    }

    private fun runTargetFileRequests(
        path: String,
        project: Project,
        parentPath: String,
        loadingRemover: Runnable?,
    ) {
        val runHttpFilePath = HttpUtils.constructFilePath(path, parentPath)

        val runFile = File(runHttpFilePath)
        if (runFile.extension != HttpFileType.DEFAULT_EXTENSION) {
            NotifyUtil.notifyWarn(project, nls("not.http.file"))

            runInEdt { loadingRemover?.run() }

            return
        }

        val runVirtualFile = HttpUtils.findVirtualFile(runHttpFilePath)
        if (runVirtualFile == null) {
            NotifyUtil.notifyWarn(project, nls("file.not.exists", runFile.absolutePath))

            runInEdt { loadingRemover?.run() }

            return
        }

        project.getService(RunHttpFileService::class.java)
            .runRequests(runVirtualFile) {
                loadingRemover?.run()

                if (it == 0) {
                    NotifyUtil.notifyInfo(project, nls("run.file.finished", runVirtualFile.name))
                }
            }
    }

}
