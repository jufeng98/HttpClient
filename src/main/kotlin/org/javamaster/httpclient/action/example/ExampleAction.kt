package org.javamaster.httpclient.action.example

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VfsUtil
import org.javamaster.httpclient.utils.HttpUtils


/**
 * @author yudong
 */
abstract class ExampleAction(text: String) : AnAction(text) {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    fun openExample(name: String) {
        val project = HttpUtils.getActiveValidProject() ?: return

        val url = javaClass.classLoader.getResource(name)!!
        val virtualFile = VfsUtil.findFileByURL(url)!!
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }
}