package org.javamaster.httpclient.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.psi.HttpGlobalHandler
import org.javamaster.httpclient.psi.HttpGlobalVariable

/**
 * @author yudong
 */
class HttpFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, HttpLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return HttpFileType.INSTANCE
    }

    fun getGlobalHandler(): HttpGlobalHandler? {
        return PsiTreeUtil.getChildOfType(this, HttpGlobalHandler::class.java)
    }

    fun getGlobalVariables(): List<HttpGlobalVariable> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpGlobalVariable::class.java)
    }

    override fun toString(): String {
        return "HTTP File"
    }
}
