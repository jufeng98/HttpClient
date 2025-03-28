package org.javamaster.httpclient.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.javamaster.httpclient.reference.provider.JsonKeyPsiReferenceProvider

/**
 *实现 Ctrl + 点击 json 属性进而跳转到 Spring 对应的 Controller 方法的出入参的 Bean 字段
 *
 * @author yudong
 */
class JsonKeyPsiReferenceContributorCoolRequest : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral::class.java),
            JsonKeyPsiReferenceProvider()
        )
    }

}
