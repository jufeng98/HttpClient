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

        val map = ScanRequest.getCacheRequestMap(module, module.project)

        return map.values
            .map {
                it.mapNotNull { innerIt ->
                    val name = innerIt.psiElement?.containingClass?.name ?: return@mapNotNull null

                    LookupElementBuilder
                        .create(innerIt.path)
                        .appendTailText("[${innerIt.method.name}]", true)
                        .withIcon(HttpIcons.REQUEST_MAPPING)
                        .withTypeText(name)
                }
            }
            .flatten()
            .toTypedArray()
    }

}
