package org.javamaster.httpclient.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.reference.provider.JsonKeyDubboServiceMethodFieldPsiReferenceProvider

/**
 * Jump to the Dubbo service method param field when pressing Ctrl + click json key
 *
 * @author yudong
 */
class JsonKeyDubboServiceMethodFieldPsiReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral::class.java),
            JsonKeyDubboServiceMethodFieldPsiReferenceProvider()
        )
    }

}
