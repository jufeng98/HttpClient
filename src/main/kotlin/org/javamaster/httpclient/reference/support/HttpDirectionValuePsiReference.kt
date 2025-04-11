package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.psi.HttpDirectionValue
import org.javamaster.httpclient.resolve.VariableResolver
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpDirectionValuePsiReference(directionValue: HttpDirectionValue, textRange: TextRange) :
    PsiReferenceBase<HttpDirectionValue>(directionValue, textRange) {

    override fun resolve(): PsiElement? {
        val parentPath = element.containingFile?.virtualFile?.parent?.path ?: return null

        var path = element.text
        if (path.length < 3) {
            return null
        }

        path = path.substring(1, path.length - 1)

        val project = element.project

        path = VariableResolver.resolveInnerVariable(path, parentPath, project)

        return HttpUtils.resolveFilePath(path, parentPath, project)
    }

}