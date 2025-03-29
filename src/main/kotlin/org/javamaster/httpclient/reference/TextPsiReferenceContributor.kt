package org.javamaster.httpclient.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiPlainTextFile
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.reference.provider.TextPsiReferenceProvider

/**
 * @author yudong
 */
class TextPsiReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiPlainTextFile::class.java), TextPsiReferenceProvider()
        )
    }

}