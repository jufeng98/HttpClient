package org.javamaster.httpclient.model

import org.javamaster.httpclient.psi.HttpDirectionComment
import java.io.File

data class PreJsFile(val directionComment: HttpDirectionComment, val file: File) {
    lateinit var content: String
}
