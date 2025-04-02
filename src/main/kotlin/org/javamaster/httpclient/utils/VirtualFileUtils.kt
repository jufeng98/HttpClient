package org.javamaster.httpclient.utils

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.*
import com.intellij.util.containers.stream
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * @author yudong
 */
object VirtualFileUtils {

    fun readNewestBytes(file: File): ByteArray {
        if (!file.exists()) {
            throw IllegalArgumentException("File ${file.absoluteFile.normalize().absolutePath} not exist!")
        }

        if (file.isDirectory) {
            throw IllegalArgumentException("${file.absoluteFile.normalize().absolutePath} not file!")
        }

        val virtualFile = VfsUtil.findFileByIoFile(file, true)!!

        val activeProject = ProjectUtil.getActiveProject()
        if (activeProject != null) {
            val opened = FileEditorManager.getInstance(activeProject).allEditors.stream()
                .anyMatch {
                    it.file == virtualFile
                }

            if (opened) {
                return virtualFile.readBytes()
            }
        }

        return Files.readAllBytes(file.toPath())
    }

    fun readNewestContent(file: File): String {
        if (!file.exists()) {
            throw IllegalArgumentException("File ${file.absoluteFile.normalize().absolutePath} not exist!")
        }

        if (file.isDirectory) {
            throw IllegalArgumentException("${file.absoluteFile.normalize().absolutePath} not file!")
        }

        val virtualFile = VfsUtil.findFileByIoFile(file, true)!!

        val activeProject = ProjectUtil.getActiveProject()
        if (activeProject != null) {
            val opened = FileEditorManager.getInstance(activeProject).allEditors.stream()
                .anyMatch {
                    it.file == virtualFile
                }

            if (opened) {
                val document = FileDocumentManager.getInstance().getDocument(virtualFile)
                return document?.text ?: virtualFile.readText()
            }
        }

        return Files.readString(file.toPath())
    }

    @JvmStatic
    fun createHttpVirtualFileFromText(
        txtBytes: ByteArray,
        suffix: String,
        project: Project,
        tabName: String?,
    ): VirtualFile {
        val date = Date()
        val tempFile: Path
        try {
            val dayStr = DateFormatUtils.format(date, "MM-dd")
            val parentDir = File(project.basePath + "/.idea/httpClient", dayStr)
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }

            val str = StringUtils.defaultString(tabName) + "-" + DateFormatUtils.format(date, "hhmmss")
            val path = Path.of(parentDir.toString(), "tmp-$str.$suffix")
            tempFile = Files.createFile(path)
            val file = tempFile.toFile()
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file.absolutePath)
            virtualFile!!.setBinaryContent(txtBytes)
            return virtualFile
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

}

