package org.javamaster.httpclient.handler

import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.application
import org.javamaster.httpclient.action.HttpAction
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.ui.HttpEditorTopForm
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.FAILED
import org.javamaster.httpclient.utils.NotifyUtil
import java.util.concurrent.TimeUnit

/**
 * @author yudong
 */
object RunFileHandler {

    fun runRequests(project: Project, topForm: HttpEditorTopForm) {
        val httpFile = PsiUtil.getPsiFile(project, topForm.file) as HttpFile
        val httpMethods = PsiTreeUtil.findChildrenOfType(httpFile, HttpMethod::class.java)

        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindowManager.getToolWindow(ToolWindowId.SERVICES)!!.show()

        application.executeOnPooledThread {
            for (it in httpMethods) {
                runInEdt {
                    if (!it.isValid) {
                        NotifyUtil.notifyCornerError(project, "Request psi element is invalid, skipped")
                        return@runInEdt
                    }

                    val action = HttpAction(it)

                    val event = AnActionEvent.createEvent(
                        action,
                        DataContext.EMPTY_CONTEXT,
                        action.templatePresentation.clone(),
                        "",
                        ActionUiKind.NONE,
                        null
                    )

                    action.actionPerformed(event)
                }

                var code = it.getUserData(HttpUtils.requestFinishedKey)
                while (code == null) {
                    Thread.sleep(600)
                    code = it.getUserData(HttpUtils.requestFinishedKey)
                }

                if (code == FAILED) {
                    runInEdt {
                        val tabName = HttpUtils.getTabName(it)
                        NotifyUtil.notifyCornerError(project, "Run $tabName failed, skipped the rest of the requests")
                    }
                    break
                }

                TimeUnit.SECONDS.sleep(3)
            }
        }
    }

}
