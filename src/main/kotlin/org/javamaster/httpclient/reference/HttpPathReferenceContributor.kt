package org.javamaster.httpclient.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.reference.support.HttpFakePsiElement
import org.javamaster.httpclient.utils.HttpUtils

/**
 * 实现 Ctrl + 点击 url 的特定部分跳转到 Spring 对应的 Controller 方法
 *
 * @author yudong
 */
class HttpPathReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(
                HttpRequestTarget::class.java
            ), HttpUrlPsiReferenceProvider()
        )
    }

    class HttpUrlPsiReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(
            element: PsiElement,
            context: ProcessingContext,
        ): Array<PsiReference> {
            val httpRequestTarget = element as HttpRequestTarget

            return createReferences(httpRequestTarget)
        }


        private fun createReferences(httpRequestTarget: HttpRequestTarget): Array<PsiReference> {
            val virtualFile = HttpUtils.getOriginalFile(httpRequestTarget) ?: return arrayOf()

            val path = virtualFile.parent?.path ?: return arrayOf()

            val pair = HttpUtils.getSearchTxtInfo(httpRequestTarget, path) ?: return arrayOf()

            val searchTxt = pair.first
            val textRange = pair.second

            val httpPsiReference = object : PsiReferenceBase<HttpRequestTarget>(httpRequestTarget, textRange) {
                override fun resolve(): PsiElement {
                    return HttpFakePsiElement(httpRequestTarget, searchTxt)
                }
            }

            return arrayOf(httpPsiReference)
        }
    }
}