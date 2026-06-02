package org.javamaster.httpclient.gutter.support

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.dashboard.HttpProgramRunner
import org.javamaster.httpclient.dashboard.HttpProgramRunner.Companion.HTTP_RUNNER_ID
import org.javamaster.httpclient.handler.RunFileHandler.runRequests
import org.javamaster.httpclient.nls.NlsBundle.nls
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpRunCommand
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

        val runCommand = element.parent as HttpRunCommand
        val path = runCommand.filePath?.text ?: return

        val containingFile = runCommand.containingFile

        if (HttpUtils.isRunTabName(path)) {
            runTargetFileRequest(path, containingFile as HttpFile, gutterComponent)
        } else {
            runTargetFileRequests(path, containingFile, gutterComponent)
        }
    }

    private fun runTargetFileRequest(name: String, httpFile: HttpFile, gutterComponent: EditorGutterComponentEx?) {
        val targetTabName = HttpUtils.getTargetTabName(name)
        if (targetTabName == null) {
            val loadingRemover = gutterComponent?.setLoadingIconForCurrentGutterMark()
            loadingRemover?.run()
            return
        }

        val pairs = MyPsiUtils.getImportFileHttpRequests(httpFile)
        val method = pairs
            .firstOrNull {
                var comment = it.first.text
                val tabName = comment.substring(3).trim()
                tabName == targetTabName
            }
            ?.second

        if (method == null) {
            NotifyUtil.notifyWarn(httpFile.project, nls("req.not.exists", targetTabName))
            val loadingRemover = gutterComponent?.setLoadingIconForCurrentGutterMark()
            loadingRemover?.run()
            return
        }

        val httpProgramRunner = ProgramRunner.findRunnerById(HTTP_RUNNER_ID)!! as HttpProgramRunner
        httpProgramRunner.executeFromGutter(method, gutterComponent)
    }

    private fun runTargetFileRequests(
        path: String,
        containingFile: PsiFile,
        gutterComponent: EditorGutterComponentEx?,
    ) {
        val loadingRemover = gutterComponent?.setLoadingIconForCurrentGutterMark()
        val project = containingFile.project
        val parentPath = containingFile.virtualFile.parent.path
        val runHttpFilePath = HttpUtils.constructFilePath(path, parentPath)

        val runFile = File(runHttpFilePath)
        if (runFile.extension != HttpFileType.DEFAULT_EXTENSION) {
            NotifyUtil.notifyWarn(project, nls("not.http.file"))
            loadingRemover?.run()
            return
        }

        val runVirtualFile = HttpUtils.findVirtualFile(runHttpFilePath)
        if (runVirtualFile == null) {
            NotifyUtil.notifyWarn(project, nls("file.not.exists", runFile.absolutePath))
            loadingRemover?.run()
            return
        }

        runRequests(project, runVirtualFile) {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val msg = "<div style='font-size:12pt'>${nls("run.file.finished", runFile.name)}!</div>"
            toolWindowManager.notifyByBalloon(ToolWindowId.SERVICES, MessageType.INFO, msg)
            loadingRemover?.run()
        }
    }

}
