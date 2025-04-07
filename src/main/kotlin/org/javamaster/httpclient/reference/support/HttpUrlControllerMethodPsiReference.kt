package org.javamaster.httpclient.reference.support

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.scan.ScanRequest
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpUrlControllerMethodPsiReference(
    private val searchTxt: String,
    private val requestTarget: HttpRequestTarget,
    textRange: TextRange,
) :
    PsiReferenceBase<HttpRequestTarget>(requestTarget, textRange) {

    override fun resolve(): PsiElement? {
        val virtualFile = element.containingFile?.virtualFile ?: return null

        val module = findModule(requestTarget, virtualFile) ?: return null

        val httpMethod = PsiTreeUtil.getPrevSiblingOfType(requestTarget, HttpMethod::class.java)!!

        return ScanRequest.findApiMethod(module, searchTxt, httpMethod.text)
    }

    private fun findModule(requestTarget: HttpRequestTarget, virtualFile: VirtualFile): Module? {
        return if (HttpUtils.isFileInIdeaDir(virtualFile)) {
            HttpUtils.getOriginalModule(requestTarget)
        } else {
            ModuleUtil.findModuleForPsiElement(requestTarget)
        }
    }

}
