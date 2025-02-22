package org.javamaster.httpclient.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.psi.HttpHeaderFieldValue
import org.javamaster.httpclient.psi.HttpVariable
import org.javamaster.httpclient.reference.support.HttpFilePathPsiReferenceProvider
import org.javamaster.httpclient.reference.support.HttpHeaderPsiReferenceProvider
import org.javamaster.httpclient.reference.support.HttpVariablePsiReferenceProvider

/**
 * @author yudong
 */
class HttpReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpVariable::class.java), HttpVariablePsiReferenceProvider()
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpFilePath::class.java), HttpFilePathPsiReferenceProvider()
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpHeaderFieldValue::class.java), HttpHeaderPsiReferenceProvider()
        )
    }
}