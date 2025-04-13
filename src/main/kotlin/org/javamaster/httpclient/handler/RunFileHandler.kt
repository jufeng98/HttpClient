package org.javamaster.httpclient.handler

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.application
import org.javamaster.httpclient.HttpRequestEnum
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
    private lateinit var finishCallback: Runnable
    private var interruptFlag = false

    fun isInterrupted(): Boolean {
        return interruptFlag
    }

    fun resetInterrupt() {
        interruptFlag = false
    }

    fun stopRunning() {
        interruptFlag = true
    }

    fun runRequests(project: Project, topForm: HttpEditorTopForm, finishCallback: Runnable) {
        this.finishCallback = finishCallback
        interruptFlag = false

        val httpFile = PsiUtil.getPsiFile(project, topForm.file) as HttpFile
        val httpMethods = PsiTreeUtil.findChildrenOfType(httpFile, HttpMethod::class.java)

        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindowManager.getToolWindow(ToolWindowId.SERVICES)!!.show()

        application.executeOnPooledThread {
            for (it in httpMethods) {
                it.putUserData(HttpUtils.requestFinishedKey, null)

                runInEdt {
                    if (!it.isValid) {
                        NotifyUtil.notifyCornerError(project, "Request psi element is invalid, skipped")
                        return@runInEdt
                    }

                    if (it.text == HttpRequestEnum.WEBSOCKET.name) {
                        NotifyUtil.notifyCornerError(project, "Skipped ws request")
                        return@runInEdt
                    }

                    val action = HttpAction(it)

                    @Suppress("removal", "DEPRECATION")
                    val event = AnActionEvent(
                        null,
                        DataContext.EMPTY_CONTEXT,
                        "",
                        action.templatePresentation.clone(),
                        ActionManager.getInstance(),
                        0
                    )

                    // Avoid the error:
                    // This method is marked with @ApiStatus.OverrideOnly annotation, which indicates that the method
                    // must be only overridden but not invoked by client code.
                    val method = action.javaClass.getDeclaredMethod("actionPerformed", AnActionEvent::class.java)
                    method.isAccessible = true
                    method.invoke(action, event)
                }

                var code = it.getUserData(HttpUtils.requestFinishedKey)
                while (code == null && !interruptFlag) {
                    Thread.sleep(600)
                    code = it.getUserData(HttpUtils.requestFinishedKey)
                }

                if (interruptFlag) {
                    break
                }


                if (code == FAILED) {
                    break
                }

                TimeUnit.SECONDS.sleep(2)
            }

            runInEdt { finishCallback.run() }
        }
    }

}
