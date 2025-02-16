package org.javamaster.httpclient.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
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

