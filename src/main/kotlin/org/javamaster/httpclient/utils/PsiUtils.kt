package org.javamaster.httpclient.utils

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType

/**
 * @author yudong
 */
object PsiUtils {
    fun resolvePsiType(psiType: PsiType?): PsiClass? {
        if (psiType !is PsiClassReferenceType) {
            return null
        }

        return psiType.resolve()
    }
}
