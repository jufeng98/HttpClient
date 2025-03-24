package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpFilePathPsiReference(httpFilePath: HttpFilePath, textRange: TextRange) :
    PsiReferenceBase<HttpFilePath>(httpFilePath, textRange) {

    override fun resolve(): PsiElement? {
        val parentPath = element.containingFile?.virtualFile?.parent?.path ?: return null

        val tmpPath = VariableResolver.resolveInnerVariable(element.text, parentPath, element.project)

        return HttpUtils.resolveFilePath(tmpPath, parentPath, element.project)
    }

}