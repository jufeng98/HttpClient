package org.javamaster.httpclient.listener

import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.util.application
import org.javamaster.httpclient.utils.FileTopUtils.clearFileStatus
import org.javamaster.httpclient.utils.FileTopUtils.initFileStatus

/**
 * @author yudong
 */
class HttpDynamicPluginListener : DynamicPluginListener {

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        for (project in ProjectUtil.getOpenProjects()) {
            val fileEditorManager = FileEditorManager.getInstance(project)

            application.executeOnPooledThread {
                fileEditorManager.openFiles.forEach {
                    clearFileStatus(fileEditorManager, it)
                }
            }
        }
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        for (project in ProjectUtil.getOpenProjects()) {
            val fileEditorManager = FileEditorManager.getInstance(project)

            application.executeOnPooledThread {
                fileEditorManager.openFiles.forEach {
                    initFileStatus(fileEditorManager, it)
                }
            }
        }
    }

}