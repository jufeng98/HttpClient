package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.javamaster.httpclient.ftp.FtpJars
import org.javamaster.httpclient.mock.MockFtpServerImpl
import org.javamaster.httpclient.mock.support.MockFtpServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.DocUtils
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * @author yudong
 */
class MockFtpProcessHandler(httpMethod: HttpMethod, selectedEnv: String?, private val port: Int) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var mockFtpServer: MockFtpServer? = null

    override fun startProcess() {
        mockServerRunningSet.add(port)

        val pair = DocUtils.createMockDoc(tabName, project)

        runInEdt {
            val oldClassLoader = Thread.currentThread().contextClassLoader

            Thread.currentThread().contextClassLoader = this.javaClass.classLoader

            try {
                loadingRemover?.run()

                httpDashboardForm.initMockServerForm(pair)

                mockFtpServer = MockFtpServerImpl(port, httpDashboardForm)

                mockFtpServer!!.startServer(paramMap)

                NotifyUtil.notifyInfo(project, NlsBundle.nls("mock.ftp.server.start", port))

                ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.SERVICES)?.show()
            } catch (e: Exception) {
                handleException(e)
            } finally {
                Thread.currentThread().contextClassLoader = oldClassLoader
            }
        }
    }

    override fun downloadOtherFiles(): Boolean {
        if (FtpJars.jarsDownloaded()) {
            return true
        }

        FtpJars.downloadAsync(project)

        runInEdt {
            httpDashboardForm.resetDashboardForm()
            loadingRemover?.run()
        }

        return false
    }


    override fun destroyProcessImpl() {
        mockServerRunningSet.remove(port)

        mockFtpServer?.stopServer()

        super.destroyProcessImpl()
    }

}