package org.javamaster.httpclient.utils

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.VfsUtil.findFileByIoFile
import com.intellij.testFramework.LightVirtualFile
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.nls.NlsBundle
import org.javamaster.httpclient.utils.HttpUtils.computeReadAction
import org.javamaster.httpclient.utils.PathUtils.legalizeFileName
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.name

/**
 * @author yudong
 */
object VirtualFileUtils {

    fun readNewestBytes(file: File): ByteArray {
        val virtualFile = HttpUtils.findVirtualFile(file.absolutePath)
        if (virtualFile != null && isVirtualFileNewest(virtualFile, file)) {
            return readNewestBytes(virtualFile)
        }

        val absolutePath = file.absoluteFile.normalize().absolutePath
        if (!file.exists()) {
            throw FileNotFoundException(absolutePath)
        }

        if (file.isDirectory) {
            throw IllegalArgumentException(NlsBundle.nls("not.file", absolutePath))
        }

        return Files.readAllBytes(file.toPath())
    }

    private fun readNewestBytes(virtualFile: VirtualFile): ByteArray {
        if (virtualFile.isDirectory) {
            throw IllegalArgumentException(NlsBundle.nls("not.file", virtualFile.path))
        }

        val document = FileDocumentManager.getInstance().getCachedDocument(virtualFile)

        return document?.text?.toByteArray() ?: virtualFile.readBytes()
    }

    fun readNewestContent(file: File): String {
        val virtualFile = HttpUtils.findVirtualFile(file.absolutePath)
        if (virtualFile != null && isVirtualFileNewest(virtualFile, file)) {
            return readNewestContent(virtualFile)
        }

        val absolutePath = file.absoluteFile.normalize().absolutePath
        if (!file.exists()) {
            throw FileNotFoundException(absolutePath)
        }

        if (file.isDirectory) {
            throw IllegalArgumentException(NlsBundle.nls("not.file", absolutePath))
        }

        return Files.readString(file.toPath())
    }

    fun readNewestContent(virtualFile: VirtualFile): String {
        if (virtualFile.isDirectory) {
            throw IllegalArgumentException(NlsBundle.nls("not.file", virtualFile.path))
        }

        val document = FileDocumentManager.getInstance().getCachedDocument(virtualFile)

        return document?.text ?: virtualFile.readText()
    }

    fun getDateHistoryDir(project: Project): File {
        val date = Date()

        val historyFolder = InnerVariableEnum.HISTORY_FOLDER.exec("", project)!!
        val dayStr = DateFormatUtils.format(date, "MM-dd")

        val parentDir = File(historyFolder, dayStr)
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        return parentDir
    }

    fun createHistoryHttpVirtualFile(project: Project, legalTabName: String): VirtualFile {
        val parentDir = getDateHistoryDir(project)

        val path = Path.of(parentDir.toString(), "$legalTabName-history.http")

        val file = if (Files.exists(path)) {
            path.toFile()
        } else {
            val tempFile = Files.createFile(path)
            tempFile.toFile()
        }

        return findFileByIoFile(file, true)!!
    }

    fun createDescListVirtualFile(
        descList: MutableList<String>,
        suffix: String,
        tabName: String,
        noLog: Boolean,
        project: Project,
    ): VirtualFile {
        val legalTabName = legalizeFileName(tabName)

        val descContent = descList.joinToString("")

        val parentDir = getDateHistoryDir(project)

        val str = legalTabName + "-" + DateFormatUtils.format(Date(), "hhmmss")
        val path = Path.of(parentDir.toString(), "$str.$suffix")

        if (noLog) {
            val lightVirtualFile = LightVirtualFile(path.name, descContent)
            return lightVirtualFile
        }

        Files.newByteChannel(path, setOf(StandardOpenOption.CREATE, StandardOpenOption.WRITE)).close()
        Files.writeString(path, descContent)

        var virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)!!

        // 当文件位于忽略列表或被排除时,此时无法构建 psi 文件
        val psiFile = computeReadAction { virtualFile.findPsiFile(project) }
        if (psiFile == null) {
            virtualFile = LightVirtualFile(virtualFile.name, descContent)
        }

        return virtualFile
    }

    private fun isVirtualFileNewest(virtualFile: VirtualFile, file: File): Boolean {
        if (!file.exists()) {
            return true
        }

        val timeStamp = virtualFile.timeStamp
        val millis = Files.getLastModifiedTime(file.toPath()).toMillis()
        return timeStamp >= millis
    }
}

