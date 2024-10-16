package org.javamaster.httpclient.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.psi.HttpHeaders
import org.javamaster.httpclient.psi.HttpUrl

/**
 * @author yudong
 */
class HttpVariableReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val provider = HttpVariablePsiReferenceProvider()

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(
                HttpUrl::class.java
            ), provider
        )
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(
                HttpHeaders::class.java
            ), provider
        )
    }
}