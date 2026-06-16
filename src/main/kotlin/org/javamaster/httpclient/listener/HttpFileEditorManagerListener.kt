package org.javamaster.httpclient.listener

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.application
import org.javamaster.httpclient.utils.FileTopUtils.clearFileStatus
import org.javamaster.httpclient.utils.FileTopUtils.initFileStatus

/**
 * @author yudong
 */
object HttpFileEditorManagerListener : FileEditorManagerListener {

    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        application.executeOnPooledThread {
            initFileStatus(source, file)
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
        clearFileStatus(source, file)
    }

}