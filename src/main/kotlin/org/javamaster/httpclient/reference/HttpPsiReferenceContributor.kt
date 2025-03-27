package org.javamaster.httpclient.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.psi.HttpFilePath
import org.javamaster.httpclient.psi.HttpHeaderFieldValue
import org.javamaster.httpclient.psi.HttpVariableArg
import org.javamaster.httpclient.psi.HttpVariableName
import org.javamaster.httpclient.reference.provider.HttpFilePathPsiReferenceProvider
import org.javamaster.httpclient.reference.provider.HttpHeaderFieldValuePsiReferenceProvider
import org.javamaster.httpclient.reference.provider.HttpVariableArgPsiReferenceProvider
import org.javamaster.httpclient.reference.provider.HttpVariableNamePsiReferenceProvider

/**
 * @author yudong
 */
class HttpPsiReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpVariableName::class.java), HttpVariableNamePsiReferenceProvider()
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpVariableArg::class.java), HttpVariableArgPsiReferenceProvider()
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpFilePath::class.java), HttpFilePathPsiReferenceProvider()
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpHeaderFieldValue::class.java), HttpHeaderFieldValuePsiReferenceProvider()
        )
    }

}