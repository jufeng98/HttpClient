package org.javamaster.httpclient.model

import org.javamaster.httpclient.psi.HttpDirectionComment
import java.io.File
import java.net.URL

data class PreJsFile(val directionComment: HttpDirectionComment, val url: URL?) {
    lateinit var file: File
    lateinit var content: String
}
