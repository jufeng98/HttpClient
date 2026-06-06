package org.javamaster.httpclient.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.psi.HttpPathAbsolute
import org.javamaster.httpclient.reference.provider.HttpPathAbsolutePsiReferenceProvider

/**
 * Jump to the Spring Controller method when pressing Ctrl + click url
 *
 * @author yudong
 */
class HttpUrlControllerMethodPsiReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpPathAbsolute::class.java),
            HttpPathAbsolutePsiReferenceProvider()
        )
    }

}