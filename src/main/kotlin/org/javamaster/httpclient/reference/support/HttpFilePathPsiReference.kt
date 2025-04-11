package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpFilePathPsiReference(httpFilePath: HttpFilePath, textRange: TextRange) :
    PsiReferenceBase<HttpFilePath>(httpFilePath, textRange) {

    override fun resolve(): PsiElement? {
        val parentPath = element.containingFile?.virtualFile?.parent?.path ?: return null

        var path = ""
        val resolvedValue = HttpUtils.getVariableResolvedValue(element.variable)
        if (resolvedValue != null) {
            path += resolvedValue
        }

        path += element.filePathContent.text

        return HttpUtils.resolveFilePath(path, parentPath, element.project)
    }

}