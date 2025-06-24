package org.javamaster.httpclient.utils

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil.findFileByIoFile
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readBytes
import com.intellij.openapi.vfs.readText
import com.intellij.testFramework.LightVirtualFile
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.javamaster.httpclient.enums.InnerVariableEnum
import org.javamaster.httpclient.nls.NlsBundle
import java.io.File
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * @author yudong
 */
object VirtualFileUtils {

    fun readNewestBytes(file: File): ByteArray {
        val virtualFile = findFileByIoFile(file, false)
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
        val virtualFile = findFileByIoFile(file, false)
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

    fun createHttpVirtualFileFromText(
        txtBytes: ByteArray,
        suffix: String,
        project: Project,
        tabName: String,
        noLog: Boolean,
    ): VirtualFile {
        val parentDir = getDateHistoryDir(project)

        val str = StringUtils.defaultString(tabName) + "-" + DateFormatUtils.format(Date(), "hhmmss")
        val path = Path.of(parentDir.toString(), "tmp-$str.$suffix")

        if (noLog) {
            val lightVirtualFile = LightVirtualFile(path.toFile().name)
            lightVirtualFile.charset = StandardCharsets.UTF_8
            lightVirtualFile.setBinaryContent(txtBytes)
            return lightVirtualFile
        }

        val file = if (Files.exists(path)) {
            path.toFile()
        } else {
            val tempFile = Files.createFile(path)
            tempFile.toFile()
        }

        val virtualFile = findFileByIoFile(file, true)
        virtualFile!!.setBinaryContent(txtBytes)

        return virtualFile
    }

    fun createHistoryHttpVirtualFile(
        content: String,
        project: Project,
        tabName: String,
    ): VirtualFile {
        val parentDir = getDateHistoryDir(project)

        val path = Path.of(parentDir.toString(), "$tabName-history.http")

        val file = if (Files.exists(path)) {
            path.toFile()
        } else {
            val tempFile = Files.createFile(path)
            tempFile.toFile()
        }

        val virtualFile = findFileByIoFile(file, true)
        virtualFile!!.setBinaryContent(content.toByteArray(StandardCharsets.UTF_8))

        return virtualFile
    }

    private fun isVirtualFileNewest(virtualFile: VirtualFile, file: File): Boolean {
        if (!file.exists()) {
            return false
        }

        val timeStamp = virtualFile.timeStamp
        val millis = Files.getLastModifiedTime(file.toPath()).toMillis()
        return timeStamp >= millis
    }
}

