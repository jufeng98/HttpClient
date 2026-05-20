package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiUtil
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.utils.HttpUtils
import org.javamaster.httpclient.utils.MyPsiUtils

/**
 * @author yudong
 */
class HttpFilePathPsiReference(httpFilePath: HttpFilePath, textRange: TextRange) :
    PsiReferenceBase<HttpFilePath>(httpFilePath, textRange) {

    override fun resolve(): PsiElement? {
        val parentPath = PsiUtil.getVirtualFile(element)?.parent?.path ?: return null
        val project = element.project

        var path = HttpUtils.resolveToActualFilePath(element)

        if (HttpUtils.isRunTabName(path)) {
            return resolveHttpRequest(path, element.containingFile as HttpFile)
        }

        return HttpUtils.resolveFilePath(path, parentPath, project)
    }

    private fun resolveHttpRequest(name: String, httpFile: HttpFile): PsiElement? {
        val targetTabName = HttpUtils.getTargetTabName(name) ?: return null

        val pairs = MyPsiUtils.getImportFileHttpRequests(httpFile)

        return pairs
            .firstOrNull {
                var comment = it.first.text
                val tabName = comment.substring(3).trim()
                tabName == targetTabName
            }
            ?.second
    }
}