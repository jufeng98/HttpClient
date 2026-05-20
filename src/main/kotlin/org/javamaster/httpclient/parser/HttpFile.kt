package org.javamaster.httpclient.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpFileType
import org.javamaster.httpclient.HttpLanguage
import org.javamaster.httpclient.psi.*

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

    fun getRequestBlocks(): List<HttpRequestBlock> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpRequestBlock::class.java)
    }

    fun getDirectionComments(): List<HttpDirectionComment> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpDirectionComment::class.java)
    }

    fun getGlobalImports(): List<HttpGlobalImport> {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, HttpGlobalImport::class.java)
    }

    override fun toString(): String {
        return "HTTP File"
    }
}
