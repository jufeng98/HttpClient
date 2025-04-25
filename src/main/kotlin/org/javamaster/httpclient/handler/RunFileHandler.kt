package org.javamaster.httpclient.handler

import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.application
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.dashboard.HttpProgramRunner
import org.javamaster.httpclient.dashboard.HttpProgramRunner.Companion.HTTP_RUNNER_ID
import org.javamaster.httpclient.nls.NlsBundle
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

        val httpProgramRunner = ProgramRunner.findRunnerById(HTTP_RUNNER_ID)!! as HttpProgramRunner

        application.executeOnPooledThread {
            for (it in httpMethods) {
                it.putUserData(HttpUtils.requestFinishedKey, null)

                runInEdt {
                    if (!it.isValid) {
                        NotifyUtil.notifyCornerError(project, NlsBundle.nls("psi.invalid"))
                        return@runInEdt
                    }

                    if (it.text == HttpRequestEnum.WEBSOCKET.name) {
                        NotifyUtil.notifyCornerError(project, NlsBundle.nls("skip.req"))
                        return@runInEdt
                    }

                    httpProgramRunner.executeFromGutter(it, null)
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

                if (it != httpMethods.last()) {
                    TimeUnit.SECONDS.sleep(2)
                }
            }

            runInEdt { finishCallback.run() }
        }
    }

}
