package org.javamaster.httpclient.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import org.javamaster.httpclient.psi.HttpRequestTarget
import org.javamaster.httpclient.reference.provider.HttpUrlControllerMethodPsiReferenceProvider

/**
 * 实现 Ctrl + 点击 url 的特定部分跳转到 Spring 对应的 Controller 方法
 *
 * @author yudong
 */
class HttpUrlControllerMethodPsiReferenceContributorCoolRequest : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpRequestTarget::class.java),
            HttpUrlControllerMethodPsiReferenceProvider()
        )
    }

}