package org.javamaster.httpclient.startup

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.javamaster.httpclient.jsPlugin.support.JavaScript
import org.javamaster.httpclient.listener.HttpFileEditorManagerListener
import org.javamaster.httpclient.logger.HttpRequestLogger.logWarn
import org.javamaster.httpclient.utils.FileTopUtils.initFileStatus


/**
 * Add a top toolbar to the http file
 *
 * @author yudong
 */
class HttpPostStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        val fileEditorManager = FileEditorManager.getInstance(project)

        fileEditorManager.openFiles.forEach {
            initFileStatus(fileEditorManager, it)
        }

        project.messageBus.connect()
            .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, HttpFileEditorManagerListener)

        try {
            if (JavaScript.isAvailable()) {
                if (JavaScript.isTsLibraryNotInstalled(project)) {
                    try {
                        JavaScript.installTsLibrary(project)
                    } catch (t: Throwable) {
                        logWarn("安装ts库错误", t)
                    }
                }

                if (JavaScript.isElementScopeNoRegister()) {
                    JavaScript.registerElementScopeProvider()
                }
            }
        } catch (t: Throwable) {
            logWarn("处理js错误", t)
        }

    }

}
