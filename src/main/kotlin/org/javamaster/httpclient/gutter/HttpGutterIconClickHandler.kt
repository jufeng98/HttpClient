package org.javamaster.httpclient.gutter

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Disposer.newDisposable
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.util.PsiTreeUtil
import org.apache.commons.lang3.exception.ExceptionUtils
import org.javamaster.httpclient.HttpInfo
import org.javamaster.httpclient.HttpRequestEnum
import org.javamaster.httpclient.js.JsScriptExecutor
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.ui.HttpExecutionConsoleToolWindow
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.HttpUtils.convertToReqBody
import org.javamaster.httpclient.utils.HttpUtils.convertToReqHeaderMap
import org.javamaster.httpclient.utils.HttpUtils.getJsScript
import org.javamaster.httpclient.utils.NotifyUtil
import org.javamaster.httpclient.utils.TooltipUtils
import org.javamaster.httpclient.ws.WsRequest
import java.io.ByteArrayInputStream
import java.io.File
import java.net.http.HttpClient.Version
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * 处理请求
 *
 * @author yudong
 */
class HttpGutterIconClickHandler(private val httpMethod: HttpMethod) {

    fun doRequest(component: EditorGutterComponentEx?, selectedEnv: String?) {
        val project = httpMethod.project
        val httpFileParentPath = httpMethod.containingFile.virtualFile.parent.path
        val tabName = HttpUtils.getTabName(httpMethod)
        try {
            // tabName会用作文件名,因此需要检测下
            Path.of(tabName)
        } catch (e: InvalidPathException) {
            NotifyUtil.notifyError(project, "参数非法,请修改!", e.message)
            return
        }


        HttpUtils.saveConfiguration(tabName, project, selectedEnv, httpMethod)

        val httpUrl = PsiTreeUtil.getNextSiblingOfType(httpMethod, HttpUrl::class.java)
        if (httpUrl == null) {
            TooltipUtils.showTooltip("url不正确!", project)
            return
        }

        val httpHeaders = PsiTreeUtil.getNextSiblingOfType(httpUrl, HttpHeaders::class.java)

        val httpBody = PsiTreeUtil.getNextSiblingOfType(httpUrl, HttpBody::class.java)

        val httpScript = PsiTreeUtil.getNextSiblingOfType(httpUrl, HttpScript::class.java)

        val httpOutputFile = PsiTreeUtil.getNextSiblingOfType(httpUrl, HttpOutputFile::class.java)

        val httpRequestEnum = HttpRequestEnum.getInstance(httpMethod)
        val jsScriptExecutor = JsScriptExecutor.getService(project)
        val variableResolver = VariableResolver.getService(project)

        jsScriptExecutor.prepareJsRequestObj()

        val httpFile = httpMethod.containingFile
        val beforeJsScripts = HttpUtils.collectBeforeJsScripts(httpFile)

        val beforeJsResList = jsScriptExecutor.evalJsBeforeRequest(beforeJsScripts)

        val httpReqDescList: MutableList<String> = mutableListOf()
        httpReqDescList.addAll(beforeJsResList)

        val version = Version.HTTP_1_1
        val url: String
        val reqHeaderMap: MutableMap<String, String>
        val reqBody: Any?
        try {
            val definitions = PsiTreeUtil.findChildrenOfType(httpFile, HttpGlobalVariableDefinition::class.java)
            variableResolver.addFileScopeVariables(definitions, selectedEnv, httpFileParentPath)

            url = variableResolver.resolve(httpUrl.text!!, selectedEnv, httpFileParentPath)

            reqHeaderMap = convertToReqHeaderMap(httpHeaders, variableResolver, selectedEnv, httpFileParentPath)
            val match = reqHeaderMap.keys.stream().anyMatch { it.equals("Content-Length") }
            if (match) {
                throw IllegalArgumentException("不能有 Content-Length 请求头!")
            }

            reqBody = convertToReqBody(httpBody, variableResolver, selectedEnv, httpFileParentPath)
        } catch (e: IllegalArgumentException) {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            toolWindowManager.notifyByBalloon(TOOL_WINDOW_ID, MessageType.ERROR, "<div>${e.message}</div>")
            return
        }

        if (url.startsWith("ws")) {
            val parentDisposer = newDisposable()
            val wsRequest = WsRequest(url, reqHeaderMap, project, parentDisposer)
            val form = HttpExecutionConsoleToolWindow()

            form.initPanelWsData(wsRequest)

            wsRequest.connect()

            initAndShowTabContent(tabName, form, parentDisposer, project, true)

            variableResolver.clearFileScopeVariables()

            return
        }

        variableResolver.clearFileScopeVariables()

        val jsScriptStr = getJsScript(httpScript)
        val parentPath = httpFile.virtualFile.parent.path
        var outPutFileName: String? = null
        if (httpOutputFile != null) {
            outPutFileName = httpOutputFile.filePath.text
        }

        val loadingRemover = component?.setLoadingIconForCurrentGutterMark()
        if (loadingRemover == null) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Sending request...", true) {
                override fun run(indicator: ProgressIndicator) {
                    val httpInfo = httpRequestEnum.execute(
                        url,
                        version,
                        reqHeaderMap,
                        reqBody,
                        jsScriptStr,
                        jsScriptExecutor,
                        httpReqDescList,
                        tabName
                    )
                    requestCompleted(project, httpInfo, null, tabName, outPutFileName, parentPath)
                }
            })
            return
        }

        CompletableFuture.supplyAsync {
            httpRequestEnum.execute(
                url,
                version,
                reqHeaderMap,
                reqBody,
                jsScriptStr,
                jsScriptExecutor,
                httpReqDescList,
                tabName
            )
        }.whenComplete { httpInfo, throwable ->
            runInEdt {
                loadingRemover.run()

                requestCompleted(project, httpInfo, throwable, tabName, outPutFileName, parentPath)
            }
        }
    }

    private fun requestCompleted(
        project: Project,
        httpInfo: HttpInfo,
        throwable: Throwable?,
        tabName: String,
        outPutFileName: String?,
        parentPath: String,
    ) {
        val jsScriptExecutor = JsScriptExecutor.getService(project)
        jsScriptExecutor.clearJsRequestObj()

        runWriteActionAndWait {
            try {
                val parentDisposer = newDisposable()

                val saveResult = saveResToFile(outPutFileName, parentPath, httpInfo.byteArray)
                if (!saveResult.isNullOrEmpty()) {
                    httpInfo.httpResDescList.add(0, saveResult)
                }

                val form = HttpExecutionConsoleToolWindow()

                form.initPanelData(httpInfo, throwable, tabName, project, parentDisposer)

                initAndShowTabContent(tabName, form, parentDisposer, project, false)
            } catch (e: Exception) {
                NotifyUtil.notifyError(project, ExceptionUtils.getStackTrace(e))
            }
        }
    }

    private fun saveResToFile(outPutFileName: String?, parentPath: String, byteArray: ByteArray?): String? {
        if (outPutFileName == null) {
            return null
        }

        if (byteArray == null) {
            return null
        }

        val path = HttpUtils.constructFilePath(outPutFileName, parentPath)
        val file = File(path)
        if (!file.parentFile.exists()) {
            Files.createDirectories(file.toPath())
        }

        if (file.exists()) {
            try {
                Files.delete(file.toPath())
            } catch (e: Exception) {
                return "# 保存失败, ${e.message?.trim()}\r\n"
            }
        }

        val inputStream = ByteArrayInputStream(byteArray)
        Files.copy(inputStream, file.toPath())

        VirtualFileManager.getInstance().asyncRefresh(null)

        return "# 响应已保存到 ${file.absolutePath}\r\n"
    }

    private fun initAndShowTabContent(
        tabName: String,
        form: HttpExecutionConsoleToolWindow,
        parentDisposer: Disposable,
        project: Project,
        wsRequest: Boolean,
    ) {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID)!!
        val contentManager = toolWindow.contentManager

        var content = contentManager.findContent(tabName)
        if (content != null) {
            content.component = form.mainPanel
        } else {
            content = contentManager.factory.createContent(form.mainPanel, tabName, false)
            content.setDisposer(parentDisposer)
        }

        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
        toolWindow.isAvailable = true

        val throwable = form.throwable
        if (throwable != null) {
            val msg = "<div style='font-size:18pt''>Tip:请求失败,${throwable.message}!</div>"
            toolWindowManager.notifyByBalloon(TOOL_WINDOW_ID, MessageType.ERROR, msg)
        } else {
            if (wsRequest) {
                toolWindow.activate { }
            } else {
                val msg = "<div style='font-size:18pt''>Tip:请求成功!</div>"
                toolWindowManager.notifyByBalloon(TOOL_WINDOW_ID, MessageType.INFO, msg)
            }
        }
    }

    companion object {
        const val TOOL_WINDOW_ID: String = "Http Execution"
    }
}
