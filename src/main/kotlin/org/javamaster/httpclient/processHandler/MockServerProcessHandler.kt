package org.javamaster.httpclient.processHandler

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ScrollingModel
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.DocumentUtil
import org.javamaster.httpclient.mock.MockServer
import org.javamaster.httpclient.mock.support.MockServerHelper
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.utils.NotifyUtil

/**
 * @author yudong
 */
class MockServerProcessHandler(httpMethod: HttpMethod, selectedEnv: String?) :
    ProcessHandlerBase(httpMethod, selectedEnv) {

    private var mockServer: MockServer? = null

    override fun startProcess() {
        val port = MockServerHelper.resolvePort(requestTarget.port)

        httpDashboardForm.initMockServerForm { editor ->
            loadingRemover?.run()

            val document = editor.document

            mockServer = MockServer(port) { log ->
                runInEdt {
                    DocumentUtil.writeInRunUndoTransparentAction {
                        document.insertString(document.textLength, log)
                        val caret: Caret = editor.caretModel.primaryCaret
                        caret.moveToOffset(document.textLength)

                        val scrollingModel: ScrollingModel = editor.scrollingModel
                        scrollingModel.scrollToCaret(ScrollType.RELATIVE)
                    }
                }
            }

            mockServer!!.startServer(request, variableResolver, paramMap)

            NotifyUtil.notifyInfo(project, NlsBundle.nls("mock.server.start", port))

            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow(ToolWindowId.SERVICES)
            toolWindow?.show()

            finishedTime = System.currentTimeMillis()
        }
    }

    override fun destroyProcessImpl() {
        mockServer?.stopServer()

        super.destroyProcessImpl()
    }
}