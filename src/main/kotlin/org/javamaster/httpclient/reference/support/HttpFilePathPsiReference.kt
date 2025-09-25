package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpFilePathPsiReference(httpFilePath: HttpFilePath, textRange: TextRange) :
    PsiReferenceBase<HttpFilePath>(httpFilePath, textRange) {

    override fun resolve(): PsiElement? {
        val parentPath = PsiUtil.getVirtualFile(element)?.parent?.path ?: return null

        var path = HttpUtils.resolveToActualFilePath(element)

        return HttpUtils.resolveFilePath(path, parentPath, element.project)
    }

}