package org.javamaster.httpclient.action.example

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VfsUtil


/**
 * @author yudong
 */
abstract class ExampleAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    fun openExample(name: String) {
        val project = ProjectUtil.getActiveProject()!!
        val url = javaClass.classLoader.getResource(name)!!
        val virtualFile = VfsUtil.findFileByURL(url)!!
        FileEditorManager.getInstance(project).openFile(virtualFile, true)
    }
}