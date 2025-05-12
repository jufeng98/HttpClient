package org.javamaster.httpclient.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.parser.HttpFile
import org.javamaster.httpclient.psi.*
import org.javamaster.httpclient.reference.provider.*

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

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpDirectionValue::class.java).withSuperParent(2, HttpFile::class.java),
            HttpDirectionValuePsiReferenceProvider()
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(HttpQueryParameterKey::class.java), HttpQueryParameterKeyPsiReferenceProvider()
        )
    }

}