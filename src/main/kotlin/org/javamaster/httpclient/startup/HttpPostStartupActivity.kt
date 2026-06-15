package org.javamaster.httpclient.startup

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.javamaster.httpclient.jsPlugin.support.JavaScript
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

        if (JavaScript.isAvailable()) {
            if (JavaScript.isTsLibraryNotInstalled(project)) {
                try {
                    JavaScript.installTsLibrary(project)
                } catch (t: Throwable) {
                    logWarn("安装ts库错误", t)
                }
            }

            if (JavaScript.isElementScopeNoRegister()) {
                try {
                    JavaScript.registerElementScopeProvider()
                } catch (t: Throwable) {
                    logWarn("注册element scope provider错误", t)
                }
            }
        }
    }

}
