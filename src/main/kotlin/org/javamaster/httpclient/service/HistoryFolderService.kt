package org.javamaster.httpclient.service

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.enums.InnerVariableEnum
import java.io.File

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class HistoryFolderService(private val project: Project) {
    @Volatile
    private var historyFolder: VirtualFile? = null

    fun getHistoryFolder(): VirtualFile? {
        if (historyFolder != null && historyFolder!!.isValid) {
            return historyFolder
        }

        val path = InnerVariableEnum.HISTORY_FOLDER.exec("", project)!!

        historyFolder = LocalFileSystem.getInstance().findFileByIoFile(File(path))

        return historyFolder
    }

    fun createHistoryFolder(): VirtualFile {
        val path = InnerVariableEnum.HISTORY_FOLDER.exec("", project)!!

        val historyPath = File(path)

        synchronized(this) {
            if (!historyPath.exists()) {
                WriteCommandAction.runWriteCommandAction(project) {
                    CommandProcessor.getInstance().runUndoTransparentAction {
                        VfsUtil.createDirectories(path)
                    }
                }
            }
        }

        historyFolder = LocalFileSystem.getInstance().findFileByIoFile(historyPath)

        return historyFolder!!
    }
}
