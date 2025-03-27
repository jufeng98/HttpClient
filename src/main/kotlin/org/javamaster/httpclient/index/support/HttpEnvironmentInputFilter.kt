package org.javamaster.httpclient.index.support

import com.intellij.json.JsonFileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import org.javamaster.httpclient.env.EnvFileService.Companion.ENV_FILE_NAMES


class HttpEnvironmentInputFilter : DefaultFileTypeSpecificInputFilter(JsonFileType.INSTANCE) {

    override fun acceptInput(file: VirtualFile): Boolean {
        return ENV_FILE_NAMES.contains(file.name)
    }

}
