package org.javamaster.httpclient.service

import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.consts.HttpConsts
import org.javamaster.httpclient.consts.HttpConsts.Companion.FAILED
import org.javamaster.httpclient.consts.HttpConsts.Companion.SUCCESS
import org.javamaster.httpclient.dashboard.HttpProgramRunner
import org.javamaster.httpclient.dashboard.HttpProgramRunner.Companion.HTTP_RUNNER_ID
import org.javamaster.httpclient.logger.HttpRequestLogger.logInfo
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.MyPsiUtils
import org.javamaster.httpclient.utils.NotifyUtil
import java.util.*
import java.util.function.Consumer

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class RunHttpFileService(private val project: Project) {
    private val httpProgramRunner = ProgramRunner.findRunnerById(HTTP_RUNNER_ID)!! as HttpProgramRunner

    fun stopRunning(virtualFile: VirtualFile) {
        virtualFile.putUserData(HttpConsts.interruptFlagKey, true)
        virtualFile.putUserData(HttpConsts.runFileKey, null)

        val runFinished = virtualFile.getUserData(HttpConsts.runFinishedKey)
        if (runFinished != null) {
            runInEdt { runFinished.accept(FAILED) }
        }
        virtualFile.putUserData(HttpConsts.runFinishedKey, null)

        logInfo("中断文件内请求: ${virtualFile.name}")
    }

    fun runRequests(virtualFile: VirtualFile, targetEnv: String?, runFinished: Consumer<Int>?) {
        logInfo("开始执行文件内所有请求:${virtualFile.name}")

        virtualFile.putUserData(HttpConsts.interruptFlagKey, null)
        virtualFile.putUserData(HttpConsts.runFinishedKey, runFinished)
        virtualFile.putUserData(HttpConsts.runFileKey, true)

        val httpMethods = HttpUtils.computeReadAction { MyPsiUtils.Companion.collectMethods(project, virtualFile) }

        val methods = LinkedList<HttpMethod>(httpMethods)

        executeRequests(methods, virtualFile, 1, targetEnv)
    }

    private fun executeRequests(
        methods: LinkedList<HttpMethod>,
        virtualFile: VirtualFile,
        idx: Int,
        targetEnv: String?,
    ) {
        val method = methods.poll()
        if (method == null) {
            handleFinished(virtualFile, SUCCESS)

            logInfo("文件内所有请求都已完成:${virtualFile.name}")

            return
        }

        if (virtualFile.getUserData(HttpConsts.interruptFlagKey) == true) {
            val tabName = HttpUtils.getTabName(method)
            logInfo("存在中断标识,直接退出,当前跳过的请求:${tabName}")
            return
        }

        val valid = HttpUtils.computeReadAction { method.isValid }
        if (!valid) {
            NotifyUtil.notifyCornerWarn(project, NlsBundle.nls("psi.invalid"))

            executeRequests(methods, virtualFile, idx, targetEnv)

            return
        }

        val methodName = method.text
        if (
            methodName == HttpRequestEnum.WEBSOCKET.name || methodName == HttpRequestEnum.MOCK_SERVER.name
            || methodName == HttpRequestEnum.MOCK_DUBBO.name || methodName == HttpRequestEnum.MOCK_WS.name
        ) {
            NotifyUtil.notifyCornerWarn(project, NlsBundle.nls("skip.req"))

            executeRequests(methods, virtualFile, idx, targetEnv)

            return
        }

        method.putUserData(HttpConsts.Companion.requestFinishedKey, Consumer {
            if (it == SUCCESS) {
                executeRequests(methods, virtualFile, idx + 1, targetEnv)
            } else {
                handleFinished(virtualFile, it)
                logInfo("请求出错了,跳过后续请求,code: $it")
            }
        })

        val tabName = HttpUtils.getTabName(method)
        logInfo("开始执行文件内请求: $tabName")

        method.putUserData(HttpConsts.runFileRequestIdxKey, idx)

        httpProgramRunner.executeFromGutter(method, null, targetEnv)
    }

    private fun handleFinished(virtualFile: VirtualFile, code: Int) {
        virtualFile.putUserData(HttpConsts.runFileKey, null)
        virtualFile.putUserData(HttpConsts.interruptFlagKey, null)

        val runFinished = virtualFile.getUserData(HttpConsts.runFinishedKey)
        runInEdt { runFinished?.accept(code) }

        virtualFile.putUserData(HttpConsts.runFinishedKey, null)
    }

}