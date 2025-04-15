package org.javamaster.httpclient.model

import com.intellij.openapi.vfs.VirtualFile
import org.javamaster.httpclient.psi.HttpDirectionComment
import java.io.File
import java.net.URL

data class PreJsFile(val directionComment: HttpDirectionComment, val url: URL?) {
    lateinit var file: File
    lateinit var virtualFile: VirtualFile
    lateinit var content: String
    val urlFile = if (url != null) File(url.toString()) else null
}
