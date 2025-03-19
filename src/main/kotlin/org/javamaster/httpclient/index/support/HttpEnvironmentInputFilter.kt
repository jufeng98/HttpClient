package org.javamaster.httpclient.index.support

import com.intellij.json.JsonFileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ArrayUtil
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import org.javamaster.httpclient.env.EnvFileService.Companion.ENV_FILE_NAMES


class HttpEnvironmentInputFilter : DefaultFileTypeSpecificInputFilter(JsonFileType.INSTANCE) {
    override fun acceptInput(file: VirtualFile): Boolean {
        return isHttpRequestEnvFile(file)
    }

    private fun isHttpRequestEnvFile(file: VirtualFile?): Boolean {
        if (file == null) {
            return false
        }

        val fileName = file.name
        return ArrayUtil.contains(fileName, *ENV_FILE_NAMES)
    }

}
