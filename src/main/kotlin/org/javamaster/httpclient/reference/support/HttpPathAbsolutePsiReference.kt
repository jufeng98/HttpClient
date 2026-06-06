package org.javamaster.httpclient.reference.support

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.javamaster.httpclient.HttpIcons
import org.javamaster.httpclient.psi.HttpPathAbsolute
import org.javamaster.httpclient.scan.ScanRequest

/**
 * @author yudong
 */
class HttpPathAbsolutePsiReference(
    private val httpPathAbsolute: HttpPathAbsolute,
    textRange: TextRange,
) :
    PsiReferenceBase<HttpPathAbsolute>(httpPathAbsolute, textRange) {

    override fun resolve(): PsiElement? {
        return null
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
