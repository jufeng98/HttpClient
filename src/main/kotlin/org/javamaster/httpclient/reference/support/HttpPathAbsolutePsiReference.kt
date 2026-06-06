package org.javamaster.httpclient.reference.support

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.psi.HttpMethod
import org.javamaster.httpclient.psi.HttpPathAbsolute
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.scan.ScanRequest
import org.javamaster.httpclient.utils.HttpUtils

/**
 * @author yudong
 */
class HttpPathAbsolutePsiReference(
    private val httpPathAbsolute: HttpPathAbsolute,
    textRange: TextRange,
) :
    PsiReferenceBase<HttpPathAbsolute>(httpPathAbsolute, textRange) {

    override fun resolve(): PsiElement? {
        val requestTarget = PsiTreeUtil.getParentOfType(httpPathAbsolute, HttpRequestTarget::class.java) ?: return null

        val httpMethod = PsiTreeUtil.getPrevSiblingOfType(requestTarget, HttpMethod::class.java) ?: return null

        val originalFile = HttpUtils.getOriginalFile(requestTarget) ?: return null

        val path = httpPathAbsolute.text
        val project = httpMethod.project
        val module = ModuleUtil.findModuleForFile(originalFile, project) ?: return null

        val scanRequest = project.getService(ScanRequest::class.java)

        return scanRequest.findSpringMvcMethod(module, path, httpMethod.text)
    }

    override fun getVariants(): Array<Any> {
        val module = ModuleUtil.findModuleForPsiElement(httpPathAbsolute) ?: return emptyArray()

        val scanRequest = module.project.getService(ScanRequest::class.java)

        val requests = scanRequest.getCacheRequestList(module)

        return requests
            .mapNotNull {
                val name = it.psiElement?.containingClass?.name ?: return@mapNotNull null

                LookupElementBuilder
                    .create(it.path)
                    .appendTailText("[${it.method.name}]", true)
                    .withIcon(HttpIcons.REQUEST_MAPPING)
                    .withTypeText(name)
            }
            .toTypedArray()
    }

}
