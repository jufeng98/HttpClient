package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.javamaster.httpclient.dubbo.support.DubboBridge
import org.javamaster.httpclient.dubbo.support.DubboJars
import org.javamaster.httpclient.mock.support.MockDubboServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.DocUtils
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import org.javamaster.httpclient.utils.NotifyUtil
import java.lang.reflect.InvocationTargetException

/**
 * @author yudong
 */
class MockDubboProcessHandler(httpMethod: HttpMethod, selectedEnv: String?, private val port: Int) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var mockDubboServer: MockDubboServer? = null

    override fun startProcess() {
        mockServerRunningSet.add(port)

        var reqHeaderMap = HttpUtils.convertToReqHeaderMap(request.header?.headerFieldList, variableResolver)
        val pair = DocUtils.createMockDoc(tabName, project)

        runInEdt {
            try {
                loadingRemover?.run()

                httpDashboardForm.initMockServerForm(pair)

                val clsName = "org.javamaster.httpclient.mock.MockDubboServerImpl"
                val mockDubboServerImpl = DubboJars.dubboClassLoader.loadClass(clsName)

                val constructor = mockDubboServerImpl.declaredConstructors[0]
                constructor.isAccessible = true

                try {
                    mockDubboServer = computeReadAction {
                        constructor.newInstance(
                            port, requestTarget.schema?.text, reqHeaderMap, DubboBridge(httpDashboardForm)
                        ) as MockDubboServer
                    }
                } catch (e: InvocationTargetException) {
                    throw e.targetException
                }

                val classLoader = Thread.currentThread().contextClassLoader
                try {
                    Thread.currentThread().contextClassLoader = DubboJars.dubboClassLoader

                    mockDubboServer!!.startServerAsync(request, variableResolver, paramMap)
                        .whenCompleteAsync { _, throwable ->
                            if (throwable != null) {
                                handleException(throwable)
                                return@whenCompleteAsync
                            }

                            NotifyUtil.notifyInfo(project, NlsBundle.nls("mock.dubbo.server.start", port))
                        }
                } finally {
                    Thread.currentThread().contextClassLoader = classLoader
                }

                ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.SERVICES)?.show()
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    override fun downloadOtherFiles(): Boolean {
        if (DubboJars.jarsDownloaded()) {
            return true
        }

        DubboJars.downloadAsync(project)

        runInEdt {
            httpDashboardForm.resetDashboardForm()
            loadingRemover?.run()
        }

        return false
    }

    override fun destroyProcessImpl() {
        mockServerRunningSet.remove(port)

        mockDubboServer?.stopServer()

        super.destroyProcessImpl()
    }

    companion object {
        private val mockServerRunningSet = mutableSetOf<Int>()

        fun isRunning(port: Int): Boolean {
            return mockServerRunningSet.contains(port)
        }
    }
}