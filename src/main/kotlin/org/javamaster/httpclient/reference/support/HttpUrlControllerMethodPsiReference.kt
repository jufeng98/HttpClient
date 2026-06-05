package org.javamaster.httpclient.reference.support

import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.scan.ScanRequest

/**
 * @author yudong
 */
class HttpUrlControllerMethodPsiReference(
    private val searchTxt: String,
    private val requestTarget: HttpRequestTarget,
    textRange: TextRange,
    private val originalFile: VirtualFile,
) :
    PsiReferenceBase<HttpRequestTarget>(requestTarget, textRange) {

    override fun resolve(): PsiElement? {
        val httpMethod = PsiTreeUtil.getPrevSiblingOfType(requestTarget, HttpMethod::class.java) ?: return null

        val module = ModuleUtil.findModuleForFile(originalFile, httpMethod.project) ?: return null

        return ScanRequest.findApiMethod(module, searchTxt, httpMethod.text)
    }

}
