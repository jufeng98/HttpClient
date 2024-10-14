package org.javamaster.httpclient.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.HttpLanguage

/**
 * @author yudong
 */
class HttpFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, HttpLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return HttpFileType.INSTANCE
    }

    override fun toString(): String {
        return "HTTP File"
    }
}
