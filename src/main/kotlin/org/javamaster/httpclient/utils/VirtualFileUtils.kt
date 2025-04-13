package org.javamaster.httpclient.utils

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.*
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * @author yudong
 */
object VirtualFileUtils {

    fun readNewestBytes(file: File): ByteArray {
        val virtualFile = VfsUtil.findFileByIoFile(file, true)
            ?: throw FileNotFoundException(file.absoluteFile.normalize().absolutePath)

        if (virtualFile.isDirectory) {
            throw IllegalArgumentException("${file.absoluteFile.normalize().absolutePath} is not file!")
        }

        val document = FileDocumentManager.getInstance().getDocument(virtualFile)

        return document?.text?.toByteArray() ?: virtualFile.readBytes()
    }

    fun readNewestContent(file: File): String {
        val virtualFile = VfsUtil.findFileByIoFile(file, true)
            ?: throw FileNotFoundException(file.absoluteFile.normalize().absolutePath)

        if (virtualFile.isDirectory) {
            throw IllegalArgumentException("${file.absoluteFile.normalize().absolutePath} is not file!")
        }

        val document = FileDocumentManager.getInstance().getDocument(virtualFile)

        return document?.text ?: virtualFile.readText()
    }

    fun createHttpVirtualFileFromText(
        txtBytes: ByteArray,
        suffix: String,
        project: Project,
        tabName: String?,
    ): VirtualFile {
        val date = Date()

        val dayStr = DateFormatUtils.format(date, "MM-dd")
        val parentDir = File(project.basePath + "/.idea/httpClient", dayStr)
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        val str = StringUtils.defaultString(tabName) + "-" + DateFormatUtils.format(date, "hhmmss")
        val path = Path.of(parentDir.toString(), "tmp-$str.$suffix")
        val tempFile = Files.createFile(path)
        val file = tempFile.toFile()

        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file.absolutePath)
        virtualFile!!.setBinaryContent(txtBytes)

        return virtualFile
    }

}

