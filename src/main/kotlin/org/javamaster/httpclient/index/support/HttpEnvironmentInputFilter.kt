package org.javamaster.httpclient.index.support

import com.intellij.json.JsonFileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ArrayUtil
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import org.javamaster.httpclient.env.EnvFileService


class HttpEnvironmentInputFilter : DefaultFileTypeSpecificInputFilter(JsonFileType.INSTANCE) {
    override fun acceptInput(file: VirtualFile): Boolean {
        return isHttpRequestEnvFile(file)
    }

    private fun isHttpRequestEnvFile(file: VirtualFile?): Boolean {
        if (file == null) {
            return false
        }

        val fileName = file.name
        return ArrayUtil.contains(fileName, *ENV_FILE_NAMES) || ArrayUtil.contains(fileName, *ENV_PRIVATE_FILE_NAMES)
    }


    companion object {
        private val ENV_FILE_NAMES: Array<String> = arrayOf(EnvFileService.ENV_FILE_NAME)
        private val ENV_PRIVATE_FILE_NAMES: Array<String> = arrayOf(EnvFileService.PRIVATE_ENV_FILE_NAME)
    }
}
