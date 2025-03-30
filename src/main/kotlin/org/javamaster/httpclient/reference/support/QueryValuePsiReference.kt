package org.javamaster.httpclient.reference.support

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * @author yudong
 */
class QueryValuePsiReference(psiElement: PsiElement, val textRange: TextRange) :
    PsiReferenceBase<PsiElement>(psiElement, textRange) {

    override fun resolve(): PsiElement? {
        return null
    }

}
