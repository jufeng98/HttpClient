package org.javamaster.httpclient.service

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.consts.HttpConsts
import java.io.File

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class CookiesFileService(private val project: Project) {
    private val historyFolderService = project.getService(HistoryFolderService::class.java)

    @Volatile
    private var cookiesFile: VirtualFile? = null

    fun getCookiesFile(): VirtualFile? {
        if (cookiesFile != null && cookiesFile!!.isValid) {
            return cookiesFile
        }

        val historyFolder = historyFolderService.getHistoryFolder() ?: return null

        val file = File(historyFolder.path, HttpConsts.COOKIE_FILE_NAME)

        cookiesFile = LocalFileSystem.getInstance().findFileByIoFile(file)

        return cookiesFile
    }

    fun createCookiesFile() {
        var historyFolder = historyFolderService.getHistoryFolder()
        if (historyFolder == null) {
            historyFolder = historyFolderService.createHistoryFolder()
        }

        val file = File(historyFolder.path, HttpConsts.COOKIE_FILE_NAME)

        synchronized(this) {
            if (!file.exists()) {
                WriteCommandAction.runWriteCommandAction(project) {
                    CommandProcessor.getInstance().runUndoTransparentAction {
                        val newFile = historyFolder.createChildData(this, HttpConsts.COOKIE_FILE_NAME)
                        VfsUtil.saveText(newFile, "# domain\tpath\tname\tvalue\tdate")
                    }
                }
            }
        }

        cookiesFile = LocalFileSystem.getInstance().findFileByIoFile(file)
    }
}
