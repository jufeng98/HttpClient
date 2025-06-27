package org.javamaster.httpclient.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.reference.provider.JsonValuePsiReferenceProvider

/**
 * Json里的所有变量声明都已脱离出来单独注入为 PlainText 类型,所以这个 Contributor 不再需要
 *
 * @author yudong
 */
class JsonValuePsiReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral::class.java),
            JsonValuePsiReferenceProvider()
        )
    }

}

