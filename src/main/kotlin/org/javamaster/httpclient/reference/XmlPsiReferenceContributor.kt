package org.javamaster.httpclient.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlToken
import org.javamaster.httpclient.reference.provider.XmlTagPsiReferenceProvider

/**
 * @author yudong
 */
class XmlPsiReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            StandardPatterns.or(
                PlatformPatterns.psiElement(XmlToken::class.java),
                PlatformPatterns.psiElement(XmlAttributeValue::class.java)
            ),
            XmlTagPsiReferenceProvider()
        )
    }

}