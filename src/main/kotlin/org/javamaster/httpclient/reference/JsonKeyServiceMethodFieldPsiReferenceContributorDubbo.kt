package org.javamaster.httpclient.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.reference.provider.JsonKeyServiceMethodFieldPsiReferenceProviderDubbo

/**
 *实现 Ctrl + 点击 json 属性进而跳转到对应的  Dubbo 方法的出入参字段
 *
 * @author yudong
 */
class JsonKeyServiceMethodFieldPsiReferenceContributorDubbo : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral::class.java),
            JsonKeyServiceMethodFieldPsiReferenceProviderDubbo()
        )
    }

}
