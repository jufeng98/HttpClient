package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import org.javamaster.httpclient.mock.MockSshServerImpl
import org.javamaster.httpclient.mock.support.MockSshServer
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.sftp.SftpJars
import org.javamaster.httpclient.utils.DocUtils
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * @author yudong
 */
class MockSshProcessHandler(httpMethod: HttpMethod, selectedEnv: String?, private val port: Int) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var mockSshServer: MockSshServer? = null

    override fun startProcess() {
        mockServerRunningSet.add(port)

        val pair = DocUtils.createMockDoc(tabName, project)

        runInEdt {
            val oldClassLoader = Thread.currentThread().contextClassLoader

            Thread.currentThread().contextClassLoader = this.javaClass.classLoader

            try {
                loadingRemover?.run()

                httpDashboardForm.initMockServerForm(pair)

                mockSshServer = MockSshServerImpl(port, httpDashboardForm)

                mockSshServer!!.startServer(paramMap)

                NotifyUtil.notifyInfo(project, NlsBundle.nls("mock.sftp.server.start", port))

                ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.SERVICES)?.show()
            } catch (e: Exception) {
                handleException(e)
            } finally {
                Thread.currentThread().contextClassLoader = oldClassLoader
            }
        }
    }

    override fun downloadOtherFiles(): Boolean {
        if (SftpJars.jarsDownloaded()) {
            return true
        }

        SftpJars.downloadAsync(project)

        runInEdt {
            httpDashboardForm.resetDashboardForm()
            loadingRemover?.run()
        }

        return false
    }


    override fun destroyProcessImpl() {
        mockServerRunningSet.remove(port)

        mockSshServer?.stopServer()

        super.destroyProcessImpl()
    }

}