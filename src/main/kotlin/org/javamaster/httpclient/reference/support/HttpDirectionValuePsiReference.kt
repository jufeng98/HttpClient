package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.psi.HttpDirectionComment
import org.javamaster.httpclient.psi.HttpDirectionValue
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpDirectionValuePsiReference(directionValue: HttpDirectionValue, textRange: TextRange) :
    PsiReferenceBase<HttpDirectionValue>(directionValue, textRange) {

    override fun resolve(): PsiElement? {
        val parentPath = PsiUtil.getVirtualFile(element)?.parent?.path ?: return null

        val directionComment = element.parent as HttpDirectionComment
        val project = element.project

        val path = HttpUtils.getDirectionPath(directionComment, parentPath) ?: return null

        return HttpUtils.resolveFilePath(path, parentPath, project)
    }

}