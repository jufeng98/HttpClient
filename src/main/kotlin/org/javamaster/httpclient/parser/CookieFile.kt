package org.javamaster.httpclient.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.CookieFileType
import org.javamaster.httpclient.CookieLanguage
import org.javamaster.httpclient.psi.CookieRecord

/**
 * @author yudong
 */
class CookieFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, CookieLanguage.INSTANCE) {

    fun getRecords(): List<CookieRecord> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, CookieRecord::class.java)
    }

    override fun getFileType(): FileType {
        return CookieFileType.INSTANCE
    }

    override fun toString(): String {
        return "Cookie File"
    }
}
