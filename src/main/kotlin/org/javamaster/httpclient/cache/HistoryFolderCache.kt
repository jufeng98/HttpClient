package org.javamaster.httpclient.cache

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.enums.InnerVariableEnum
import java.io.File

/**
 * @author yudong
 */
@Service(Service.Level.PROJECT)
class HistoryFolderCache(private val project: Project) {
    @Volatile
    private var historyFolder: VirtualFile? = null

    fun getHistoryFolder(): VirtualFile? {
        if (historyFolder != null && historyFolder!!.isValid) {
            return historyFolder
        }

        val ideaDir = InnerVariableEnum.HISTORY_FOLDER.exec("", project)!!

        historyFolder = ReadAction.compute<VirtualFile?, Throwable> {
            VfsUtil.findFileByIoFile(File(ideaDir), false)
        }

        return historyFolder
    }
}
